package net.benoodle.eorder;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
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
import java.util.ArrayList;
import in.goodiebag.carouselpicker.CarouselPicker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
//import static net.benoodle.eorder.retrofit.UtilsApi.BASE_URL_API;

public class MainActivity extends AppCompatActivity implements MainAdaptador.ComprarListener {
    /*Alternativa a declararlo como static, Node implementa parcelable.
    bundle.putParcelableArrayList("key", Node);
    getParcelableArrayList("key);
    Para pasar entre actividades
     */

    public static final String MENU = "menu";
    private final String INICIO_TYPE = "menu"; //El tipo que se mostrará al principio
    public static final int REQUEST_CODE = 1;
    public static Catalog catalog;
    public static Order order;
    private ArrayList<Tipo> tipos = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private MainAdaptador adaptador;
    private SharedPrefManager sharedPrefManager;
    private ApiService mApiService;
    private String URL;
    //private CarouselPicker carouselPicker;
    private Context context;
    //private HashMap<String, ImageView> typesImages = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        //Ejectuar la app en modo quiosco, necesita device owner via adb
        //dpm set-device-owner net.benoodle.eorder/.MyAdmin
        //http://wenchaojiang.github.io/blog/realise-Android-kiosk-mode/
        DevicePolicyManager myDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName mDPM = new ComponentName(this, MyAdmin.class);
        if (myDevicePolicyManager.isDeviceOwnerApp(this.getPackageName())) {
            String[] packages = {this.getPackageName()};
            myDevicePolicyManager.setLockTaskPackages(mDPM, packages);
        } else {
            Toast.makeText(getApplicationContext(),"Not owner", Toast.LENGTH_LONG).show();
        }
        startLockTask();
        this.context = getApplicationContext();
        //carouselPicker = findViewById(R.id.carousel);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        sharedPrefManager = new SharedPrefManager(this);
        this.URL = sharedPrefManager.getSPUrl();
        mApiService = UtilsApi.getAPIService(this.URL);
        order = new Order (sharedPrefManager.getSPStore());
        if (!sharedPrefManager.getSPIsLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        /*Cargar todos los tipos de productos, aunque no haya productos en stock para algunas categorías.
         Esto se hace por si volviera a haber stock tener las categorías cargadas en memoria desde incio
        */
        mApiService.getTypes(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Typescallback);
        //mApiService.getAllNodes(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Nodecallback);
    }

    protected void onStart() {
        super.onStart();
        //Si recargo el catálogo me machaca el stock, no hay que hacerlo hasta finalizar la compra.
        //mApiService.getAllNodes(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Nodecallback);
    }

    public void ShowCart(View v) {
        /*Intent intent = new Intent(this, CartActivity.class);
        this.startActivity(intent);*/
        //Si el resultado es 1 es compra exitosa, recargar el catálogo. Así no machaca el stock del carrito en compras a medias.
        startActivityForResult(new Intent(this, CartActivity.class), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mApiService.getAllNodes(sharedPrefManager.getSPStore(), sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Nodecallback);
            }else{
                adaptador.notifyDataSetChanged();
            }
        }
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
                lp.setMargins(0, 0, 0, 16);
                //Altura del layout para dividir el espacio entre títulos e imágenes
                Float height = new Float(typesLayout.getHeight());
                Double imagesHeight = height*0.75;
                Double textHeight = height*0.25;
                catalog = new Catalog(response.body());
                catalog.CrearTypes();
                /*if (order.getOrderItems().size() > 0) {
                    if (catalog.sincronizarStock(order)){
                        Toast.makeText(context, R.string.no_stock, Toast.LENGTH_SHORT);
                    }
                }*/
                typesAvaliable = catalog.getTypes();
                for (String name : typesAvaliable){
                    for (Tipo tipo : tipos){
                        if (tipo.getName().compareTo(name) == 0){
                            LinearLayout titlesLayout = new LinearLayout(context);
                            titlesLayout.setOrientation(LinearLayout.VERTICAL);
                            titlesLayout.setLayoutParams(lp);
                            titlesLayout.setGravity(Gravity.CENTER);
                            titlesLayout.setPadding(60, 0, 60, 0);
                            ImageView image = new ImageView(context);
                            image.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    imagesHeight.intValue()));
                            image.setId(typesAvaliable.indexOf(name));
                            image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                   String tipo = typesAvaliable.get(v.getId());
                                   adaptador = new MainAdaptador(catalog.TypeCatalog(tipo), MainActivity.this,   MainActivity.this);
                                   recyclerView.setAdapter(adaptador);
                                }
                            });
                            //Modo sin setIndicatorsEnabled para ver si es de internet o de caché
                            Picasso.with(context).load(URL+tipo.getUrl()).resize(0, typesLayout.getHeight()).into(image);
                            //Modo con SetIndicatorsEnabled ROJO Network, AZUL disk, VERDE memory
                            //Picasso mPicasso = Picasso.with(context);
                            //mPicasso.setIndicatorsEnabled(true);
                            //Modo normal de Picasso
                            //mPicasso.load(BASE_URL_API+tipo.getUrl()).resize(0, typesLayout.getHeight()).into(image);
                            //mPicasso.load(BASE_URL_API+tipo.getUrl()).resize(0, imagesHeight.intValue()).into(image);
                            titlesLayout.addView(image);
                            /*Probar con textview para los títulos de las categorías*/
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
            Toast.makeText(MainActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    Callback<ArrayList<Tipo>> Typescallback = new Callback<ArrayList<Tipo>>() {
        @Override
        public void onResponse(Call<ArrayList<Tipo>> call, Response<ArrayList<Tipo>> response) {
            if (response.isSuccessful()) {
                tipos = response.body();
                mApiService.getAllNodes(sharedPrefManager.getSPStore(), sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Nodecallback);
            }
        }
        @Override
        public void onFailure(Call<ArrayList<Tipo>> call, Throwable t) {
            Toast.makeText(MainActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

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
            try{
                order.addOrderItem(node.getProductID(), quantity);
                Toast.makeText(getApplicationContext(), R.string.product_added, Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), R.string.no_sell, Toast.LENGTH_SHORT).show();
            }
        }
        adaptador.notifyDataSetChanged();
    }
}