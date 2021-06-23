package net.benoodle.eorder;

import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import net.benoodle.eorder.model.Node;
import net.benoodle.eorder.model.OrderItem;
import net.benoodle.eorder.model.Tipo;
import net.benoodle.eorder.retrofit.ApiService;
import net.benoodle.eorder.retrofit.SharedPrefManager;
import net.benoodle.eorder.retrofit.UtilsApi;
import java.util.ArrayList;
import java.util.Locale;

import static net.benoodle.eorder.TypesActivity.catalog;
import static net.benoodle.eorder.TypesActivity.tipos;
import static net.benoodle.eorder.TypesActivity.order;

public class MainActivity extends AppCompatActivity implements MainAdaptador.ComprarListener {

    public static final int REQUEST_CODE = 1;
    public static String MENU = "menu";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private MainAdaptador adaptador;
    private SharedPrefManager sharedPrefManager;
    private Context context;
    private String type;
    private LinearLayout typesLayout, resumenLayout;
    private TextView total;
    private ArrayList<String> typesAvaliable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = getApplicationContext();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        sharedPrefManager = new SharedPrefManager(this);
        if (!sharedPrefManager.getSPIsLoggedIn()) {
            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        this.type = getIntent().getStringExtra("type");
        this.resumenLayout = findViewById(R.id.resumen);
        this.total = findViewById(R.id.total);
        typesLayout = findViewById(R.id.main_types_layout);
        typesLayout.removeAllViews();
        typesLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                typesLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                cargarTypes();
            }
        });
        adaptador = new MainAdaptador(catalog.TypeCatalog(this.type), MainActivity.this, MainActivity.this);
        recyclerView.setAdapter(adaptador);
    }

    public void onResume(){
        super.onResume();
        actualizarResumen();
    }

    public void ShowCart(View v) {
        //Si el resultado es 1 es compra exitosa, recargar el catálogo. Así no machaca el stock del carrito en compras a medias.
        startActivityForResult(new Intent(this, CartActivity.class), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                finish();
            } else {
                adaptador.notifyDataSetChanged();
            }
        }
    }

    public void cargarTypes() {
        Float height = (float) findViewById(R.id.main_types_scroll).getHeight();
        double imagesHeight = height * 0.60;
        double textHeight = height * 0.25;
        typesAvaliable = catalog.getTypes();
        for (String name : typesAvaliable) {
            for (Tipo tipo : tipos) {
                if (tipo.getId().compareTo(name) == 0) {
                    LinearLayout titlesLayout = new LinearLayout(context);
                    titlesLayout.setOrientation(LinearLayout.VERTICAL);
                    titlesLayout.setMinimumHeight(height.intValue());
                    titlesLayout.setGravity(Gravity.CENTER);
                    titlesLayout.setPadding(30, 0, 30, 0);
                    ImageView image = new ImageView(context);
                    image.setMaxHeight((int) imagesHeight);
                    image.setId(typesAvaliable.indexOf(name));
                    image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String tipo = typesAvaliable.get(v.getId());
                            adaptador = new MainAdaptador(catalog.TypeCatalog(tipo), context, MainActivity.this);
                            recyclerView.setAdapter(adaptador);
                        }
                    });
                    try{
                        Picasso.with(context).load(tipo.getUrl()).resize(0, (int) imagesHeight).into(image);
                    } catch (Exception e){
                        Picasso.with(context).load(tipo.getUrl()).into(image);
                    }

                    titlesLayout.addView(image);
                    TextView text = new TextView(context);
                    text.setText(tipo.getName());
                    text.setHeight((int) textHeight);
                    text.setMinHeight((int) textHeight);
                    text.setGravity(Gravity.CENTER);
                    titlesLayout.addView(text);
                    typesLayout.addView(titlesLayout);
                }
            }
        }
    }

    /*
    Añade al carrito el producto y la cantidad  pasadas.
    Si es un menú pide al usuario las opciones en MenuActivity
    Las opciones vienen de node.productos[] del server.
     */
    @Override
    public void Anadir(Node node, int quantity) {
        if (node.getType().equals(MENU)) {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("id", node.getProductID());
            //Con el result llamaremos a adaptador.notifyDataSetChange para que cambie el stock o no
            startActivityForResult(intent, 1);
        } else if (!node.getType().equals(MENU)) {
            try {
                order.addOrderItem(node.getProductID(), quantity);
                Toast.makeText(context, getResources().getString(R.string.product_added), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, getResources().getString(R.string.no_sell), Toast.LENGTH_SHORT).show();
            }
        }
        adaptador.notifyDataSetChanged();
        actualizarResumen();
    }

    public void actualizarResumen() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(8, 0, 0, 0);
        resumenLayout.removeAllViews();
        for (OrderItem orderItem : order.getOrderItems()) {
            TextView text = new TextView(context);
            try {
                final Node node = catalog.getNodeById(orderItem.getProductID());
                text.setText(node.getTitle() + " " + orderItem.getQuantity());
            } catch (Exception e) {
                text.setText(e.getMessage());
            }
            text.setLayoutParams(lp);
            text.setAutoSizeTextTypeUniformWithConfiguration(2, 100, 2, TypedValue.COMPLEX_UNIT_DIP);
            TextViewCompat.setAutoSizeTextTypeWithDefaults(text, TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
            resumenLayout.addView(text);
        }
        try {
            total.setText(String.format("%s %s €", getResources().getString(R.string.total), String.format(Locale.getDefault(), "%.2f", order.getTotal())));
        } catch (Exception e) {
            total.setText(e.getLocalizedMessage());
        }
    }
}