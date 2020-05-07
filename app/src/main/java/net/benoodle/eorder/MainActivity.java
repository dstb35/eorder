package net.benoodle.eorder;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import net.benoodle.eorder.model.Catalog;
import net.benoodle.eorder.model.Node;
import net.benoodle.eorder.model.Tipo;
import net.benoodle.eorder.retrofit.ApiService;
import net.benoodle.eorder.retrofit.SharedPrefManager;
import net.benoodle.eorder.retrofit.UtilsApi;
import net.benoodle.eorder.model.Order;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import in.goodiebag.carouselpicker.CarouselPicker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.benoodle.eorder.retrofit.UtilsApi.BASE_URL_API;

public class MainActivity extends AppCompatActivity implements MainAdaptador.ComprarListener {
    /*Alternativa a declararlo como static, Node implementa parcelable.
    bundle.putParcelableArrayList("key", Node);
    getParcelableArrayList("key);
    Para pasar entre actividades
     */

    public static final String MENU = "Menús";
    private final String INICIO_TYPE = "Menús"; //El tipo que se mostrará al principio
    public static Catalog catalog;
    public static Order order = new Order();
    private ArrayList<Tipo> tipos = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private MainAdaptador adaptador;
    private SharedPrefManager sharedPrefManager;
    private ApiService mApiService;
    private CarouselPicker carouselPicker;
    private Context context;
    //private HashMap<String, ImageView> typesImages = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = getApplicationContext();
        carouselPicker = findViewById(R.id.carousel);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        mApiService = UtilsApi.getAPIService();
        sharedPrefManager = new SharedPrefManager(this);
        if (!sharedPrefManager.getSPIsLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        /*Cargar todos los tipos de productos, aunque no haya productos en stock para algunas categorías.
         Esto se hace por si volviera a haber stock tener las categorías cargadas en memoria desde incio
        */
        mApiService.getTypes(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Typescallback);
    }

    protected void onStart() {
        super.onStart();
        mApiService.getAllNodes(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Nodecallback);
    }

    public void ShowCart(View v) {
        Intent intent = new Intent(this, CartActivity.class);
        this.startActivity(intent);
    }

    /*Lanza las preferencias de la app, pero están deshabilitado el botón ahora*/
    public void LanzarPreferencias(View v) {
        Intent intent = new Intent(this, PreferenciasActivity.class);
        this.startActivity(intent);
    }

    Callback<ArrayList<Node>> Nodecallback = new Callback<ArrayList<Node>>() {
        @Override
        public void onResponse(Call<ArrayList<Node>> call, Response<ArrayList<Node>> response) {
            final ArrayList<String> typesAvaliable;
            if (response.isSuccessful()) {
                LinearLayout typesLayout;
                typesLayout = findViewById(R.id.types);
                typesLayout.removeAllViews();
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                //lp.gravity = Gravity.CENTER;

                //Altura del layout para dividir el espacio entre títulos e imágenes
                Float height = new Float(typesLayout.getHeight());
                Double imagesHeight = height*0.75;
                Double textHeight = height*0.25;
                catalog = new Catalog(response.body());
                catalog.CrearTypes();
                typesAvaliable = catalog.getTypes();
                for (String name : typesAvaliable){
                    for (Tipo tipo : tipos){
                        if (tipo.getName().compareTo(name) == 0){
                            LinearLayout titlesLayout = new LinearLayout(context);
                            titlesLayout.setOrientation(LinearLayout.VERTICAL);
                            titlesLayout.setLayoutParams(lp);
                            titlesLayout.setGravity(Gravity.CENTER);
                            titlesLayout.setPadding(60, 0, 60, 0);
                            //titlesLayout.removeAllViews();
                            ImageView image = new ImageView(context);
                            image.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    imagesHeight.intValue()));
                            image.setId(typesAvaliable.indexOf(name));
                            //image.setPadding(30, 0, 30, 0);
                            image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                   String tipo = typesAvaliable.get(v.getId());
                                   adaptador = new MainAdaptador(catalog.TypeCatalog(tipo), MainActivity.this, MainActivity.this);
                                   recyclerView.setAdapter(adaptador);
                                }
                            });
                            /*
                            Modo sin setIndicatorsEnabled para ver si es de internet o de caché
                            Picasso.with(context).load(BASE_URL_API+tipo.getURL()).resize(0, linearLayout.getHeight()).into(image);
                            */
                            //Modo con SetIndicatorsEnabled ROJO Network, AZUL disk, VERDE memory
                            Picasso mPicasso = Picasso.with(context);
                            mPicasso.setIndicatorsEnabled(true);
                            //mPicasso.load(BASE_URL_API+tipo.getUrl()).resize(0, typesLayout.getHeight()).into(image);
                            mPicasso.load(BASE_URL_API+tipo.getUrl()).resize(0, imagesHeight.intValue()).into(image);
                            titlesLayout.addView(image);

                            /*Probar con textview para los títulos de las categorías*/
                            TextView text = new TextView(context);
                            text.setText(tipo.getName());
                            text.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    textHeight.intValue()));
                            text.setGravity(Gravity.CENTER);
                            TextViewCompat.setAutoSizeTextTypeWithDefaults(text, TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                            /*text.setGravity(Gravity.CENTER);
                            text.setPadding(30, 0, 30, 0);*/
                            titlesLayout.addView(text);
                            typesLayout.addView(titlesLayout);
                        }
                    }

                }
                /*List<CarouselPicker.PickerItem> itemsImages = new ArrayList<>();
                for (String tipo : types) {
                    /*int id = MainActivity.this.getResources().getIdentifier(tipo.toLowerCase(), "drawable", MainActivity.this.getPackageName());
                    itemsImages.add(new CarouselPicker.DrawableItem(id));
                }*/
            }

            /*carouselPicker.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    String tipo = types.get(position);
                    adaptador = new MainAdaptador(catalog.TypeCatalog(tipo), MainActivity.this, MainActivity.this);
                    recyclerView.setAdapter(adaptador);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            CarouselPicker.CarouselViewAdapter imageAdapter = new CarouselPicker.CarouselViewAdapter(MainActivity.this, itemsImages, 0);
            carouselPicker.setAdapter(imageAdapter);*/
            adaptador = new MainAdaptador(catalog.TypeCatalog(INICIO_TYPE), MainActivity.this, MainActivity.this);
            recyclerView.setAdapter(adaptador);

        }

        @Override
        public void onFailure(Call<ArrayList<Node>> call, Throwable t) {
            t.printStackTrace();
        }
    };

    Callback<ArrayList<Tipo>> Typescallback = new Callback<ArrayList<Tipo>>() {
        @Override
        public void onResponse(Call<ArrayList<Tipo>> call, Response<ArrayList<Tipo>> response) {
            if (response.isSuccessful()) {
                tipos = response.body();
            }
        }
        @Override
        public void onFailure(Call<ArrayList<Tipo>> call, Throwable t) {
            t.printStackTrace();
        }
    };

        /*
    Añade al carrito el producto y la cantidad  pasadas.
    Si es un menú pide al usuario las opciones en MenuActivity
    Las opciones vienen de node.productos[] del server.
     */
    @Override
    public void Añadir(Node node, int cantidad) {
        if (node.getType().equals(MENU)) {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("sku", node.getSku());
            this.startActivity(intent);
        } else if (!node.getType().equals(MENU)) {
            order.addOrderItem(node.getSku(), cantidad, node.getTitle());
            Toast.makeText(getApplicationContext(), R.string.product_added, Toast.LENGTH_SHORT).show();
        }
    }
}