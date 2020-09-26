package net.benoodle.eorder;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import java.util.Locale;

import in.goodiebag.carouselpicker.CarouselPicker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
//import static net.benoodle.eorder.retrofit.UtilsApi.BASE_URL_API;

public class TypesActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Context context;
    public static Order order;
    private SharedPrefManager sharedPrefManager;
    private String URL;
    private ApiService mApiService;
    public static ArrayList<Tipo> tipos = new ArrayList<>();
    public static Catalog catalog;
    private float screenWidth, screenHeight;
    private Spinner spinner;
    private Locale myLocale;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_types);

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
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        this.screenHeight = displayMetrics.heightPixels /  displayMetrics.density;
        this.screenWidth = displayMetrics.widthPixels / displayMetrics.density;
        sharedPrefManager = new SharedPrefManager(this);
        this.URL = sharedPrefManager.getSPUrl();
        this.order = new Order(sharedPrefManager.getSPStore());
        mApiService = UtilsApi.getAPIService(this.URL);
        if (!sharedPrefManager.getSPIsLoggedIn()) {
            Intent intent = new Intent(TypesActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        spinner = (Spinner) findViewById(R.id.spinner);
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        String lang= parent.getItemAtPosition(pos).toString();
        String languageCode =""  ;
        switch (lang){
            case "Español":
                languageCode = "es";
                break;
            case "Català":
                languageCode = "ca";
                break;
            case "English":
                languageCode = "en";
                break;
        }
        changeLang(languageCode);

    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void onResume(){
        super.onResume();
        /*Cargar todos los tipos de productos, aunque no haya productos en stock para algunas categorías.
         Esto se hace por si volviera a haber stock tener las categorías cargadas en memoria desde incio
        */
        mApiService.getTypes(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Typescallback);
    }

    public void changeLang(String languageCode){
        /*Locale locale = new Locale(languageCode);
        Configuration config = getBaseContext().getResources().getConfiguration();
        config.setLocale(locale);
        //context.createConfigurationContext(config);
        config.locale = locale;
        context.update
        config.updateConfiguration(config, res.getDisplayMetrics());*/
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
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
                LinearLayout fourTypesLayout = new LinearLayout(context);
                typesLayout = findViewById(R.id.types);
                typesLayout.removeAllViews();
                //lp.setMargins(0, 0, 0, 16);
                //Altura del layout para dividir el espacio entre títulos e imágenes
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                Double imagesWidth = new Float (displayMetrics.widthPixels)*0.20;
                Double imagesHeight = new Float (displayMetrics.heightPixels)*0.45;
                //Float width = new Float(typesLayout.getWidth());
                //Float height = new Float(typesLayout.getHeight()*0.30);
                //Double fourTypesheight = screenHeight*0.25;
                //Double imagesWidth = screenWidth*0.25;
                //Double fourTypesheight = height*0.25;
                //Double imagesWidth = width*0.15;
                LinearLayout.LayoutParams titlesParams = new LinearLayout.LayoutParams(
                        imagesWidth.intValue(),
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                titlesParams.setMargins(15, 0, 15, 0);
                catalog = new Catalog(response.body());
                catalog.CrearTypes();
                typesAvaliable = catalog.getTypes();
                for (int i=0; i<typesAvaliable.size(); i++){
                    final String name = typesAvaliable.get(i);
                    for (Tipo tipo : tipos){
                        if (tipo.getId().compareTo(name) == 0){
                            if (i%4 == 0){
                                fourTypesLayout = new LinearLayout(context);
                                fourTypesLayout.setOrientation(LinearLayout.HORIZONTAL);
                                fourTypesLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        imagesHeight.intValue()
                                ));
                                fourTypesLayout.setGravity(Gravity.CENTER);
                                typesLayout.addView(fourTypesLayout);
                            }
                            LinearLayout titleLayout = new LinearLayout(context);
                            titleLayout.setOrientation(LinearLayout.VERTICAL);
                            titleLayout.setLayoutParams(titlesParams);
                            /*titleLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                    imagesWidth.intValue(),
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            ));*/
                            titleLayout.setGravity(Gravity.CENTER);
                            //titleLayout.setPadding(180, 0, 180, 0);
                            /*Float imageHeight = new Float(titleLayout.getHeight());
                            Double dimageHeight = imageHeight*0.75;*/
                            ImageView image = new ImageView(context);
                            image.setId(typesAvaliable.indexOf(name));
                            image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(context, MainActivity.class);
                                    intent.putExtra("type", name);
                                    startActivity(intent);
                                }
                            });
                            Picasso.with(context).load(URL+tipo.getUrl()).resize(imagesWidth.intValue(), 0).into(image);
                            titleLayout.addView(image);
                            TextView text = new TextView(context);
                            text.setText(tipo.getName());
                            text.setWidth(imagesWidth.intValue());
                            text.setMinHeight(80);
                            /*text.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));*/
                            text.setGravity(Gravity.CENTER);
                            //TextViewCompat.setAutoSizeTextTypeWithDefaults(text, TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(text, 24, 100, 2, TypedValue.COMPLEX_UNIT_SP);
                            titleLayout.addView(text);
                            fourTypesLayout.addView(titleLayout);
                        }
                    }
                }
            }
        }

        @Override
        public void onFailure(Call<ArrayList<Node>> call, Throwable t) {
            Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

}
