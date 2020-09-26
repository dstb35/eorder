package net.benoodle.eorder;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import static net.benoodle.eorder.TypesActivity.order;

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

import net.benoodle.eorder.model.Catalog;
import net.benoodle.eorder.model.Node;
import net.benoodle.eorder.model.OrderItem;
import net.benoodle.eorder.model.Tipo;
import net.benoodle.eorder.retrofit.ApiService;
import net.benoodle.eorder.retrofit.SharedPrefManager;
import net.benoodle.eorder.retrofit.UtilsApi;
import net.benoodle.eorder.model.Order;

import java.util.ArrayList;

import in.goodiebag.carouselpicker.CarouselPicker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.benoodle.eorder.TypesActivity.catalog;
import static net.benoodle.eorder.TypesActivity.tipos;

public class MainActivity extends AppCompatActivity implements MainAdaptador.ComprarListener {

    public static final int REQUEST_CODE = 1;
    public static String MENU = "menu";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private MainAdaptador adaptador;
    private SharedPrefManager sharedPrefManager;
    private ApiService mApiService;
    private String URL;
    private Context context;
    private String type;
    private LinearLayout typesLayout, resumenLayout;
    private TextView total;
    private ArrayList<String> typesAvaliable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        this.context = getApplicationContext();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        sharedPrefManager = new SharedPrefManager(this);
        this.URL = sharedPrefManager.getSPUrl();
        mApiService = UtilsApi.getAPIService(this.URL);
        if (!sharedPrefManager.getSPIsLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        this.type = getIntent().getStringExtra("type");
        this.resumenLayout = findViewById(R.id.resumen);
        this.total = findViewById(R.id.total);
        typesLayout = findViewById(R.id.types);
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
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(0, 0, 0, 16);
        //Altura del layout para dividir el espacio entre títulos e imágenes
        Float height = new Float(typesLayout.getHeight());
        Double imagesHeight = height * 0.75;
        Double textHeight = height * 0.20;
        typesAvaliable = catalog.getTypes();
        for (String name : typesAvaliable) {
            for (Tipo tipo : tipos) {
                if (tipo.getId().compareTo(name) == 0) {
                    LinearLayout titlesLayout = new LinearLayout(context);
                    titlesLayout.setOrientation(LinearLayout.VERTICAL);
                    titlesLayout.setLayoutParams(lp);
                    titlesLayout.setGravity(Gravity.CENTER);
                    titlesLayout.setPadding(10, 5, 0, 0);
                    ImageView image = new ImageView(context);
                    image.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            imagesHeight.intValue()));
                    image.setId(typesAvaliable.indexOf(name));
                    image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String tipo = typesAvaliable.get(v.getId());
                            adaptador = new MainAdaptador(catalog.TypeCatalog(tipo), MainActivity.this, MainActivity.this);
                            recyclerView.setAdapter(adaptador);
                        }
                    });
                    Picasso.with(context).load(URL + tipo.getUrl()).resize(0, typesLayout.getHeight()).into(image);
                    titlesLayout.addView(image);
                    TextView text = new TextView(context);
                    text.setText(tipo.getName());
                    text.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            textHeight.intValue()));
                    text.setGravity(Gravity.CENTER);
                    TextViewCompat.setAutoSizeTextTypeWithDefaults(text, TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
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
    public void Añadir(Node node, int quantity) {
        if (node.getType().equals(MENU)) {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("id", node.getProductID());
            //this.startActivity(intent);
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
            //text.setGravity(Gravity.CENTER);
            //text.setTextSize(14);
            text.setAutoSizeTextTypeUniformWithConfiguration(10, 100, 2, TypedValue.COMPLEX_UNIT_DIP);
            TextViewCompat.setAutoSizeTextTypeWithDefaults(text, TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
            resumenLayout.addView(text);
        }
        //TextView total = new TextView(context);
        try {
            //total.setText(System.getProperty("line.separator")+String.format("%s %s €", getResources().getString(R.string.total), String.format("%.2f", order.getTotal())));
            //total.setTextSize(30);
            //total.setLayoutParams(lp);
            //resumenLayout.addView(total);
            total.setText(String.format("%s %s €", getResources().getString(R.string.total), String.format("%.2f", order.getTotal())));
        } catch (Exception e) {
            total.setText(e.getLocalizedMessage());
        }
    }
}