package net.benoodle.eorder;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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

import android.app.admin.DeviceAdminReceiver;

import org.json.JSONObject;

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
    private Spinner spinner;
    private String langCode;
    private LinearLayout typesLayout;
    private ArrayList<String> typesAvaliable;
    private boolean check;

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
            Toast.makeText(getApplicationContext(), "Not owner", Toast.LENGTH_LONG).show();
        }
        startLockTask();
        this.context = getApplicationContext();
        sharedPrefManager = new SharedPrefManager(this);
        if (!sharedPrefManager.getSPIsLoggedIn()) {
            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        this.URL = sharedPrefManager.getSPUrl();
        this.order = new Order(sharedPrefManager.getSPStore());
        this.langCode = Locale.getDefault().getLanguage();
        this.check = false;
        mApiService = UtilsApi.getAPIService(this.URL);
        spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        typesLayout = findViewById(R.id.types_layout);

    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (check) {
            langCode = getResources().getStringArray(R.array.langCodes)[pos];
        }else{
            check = true;
            String[] langCodes = getResources().getStringArray(R.array.langCodes);
            if (langCode != null) {
                for (int i=0; i<langCodes.length; i++){
                    if (langCode.compareTo(langCodes[i]) == 0){
                        int spinnerPosition = i;
                        spinner.setSelection(spinnerPosition);
                        break;
                    }
                }
            }
        }
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        langCode = Locale.getDefault().getLanguage();
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
        mApiService.getTypes(sharedPrefManager.getSPBasicAuth(), langCode, sharedPrefManager.getSPCsrfToken()).enqueue(Typescallback);

    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void onRestart() {
        /*Cargar todos los tipos de productos, aunque no haya productos en stock para algunas categorías.
         Esto se hace por si volviera a haber stock tener las categorías cargadas en memoria desde incio
        */
        super.onRestart();
        mApiService.getTypes(sharedPrefManager.getSPBasicAuth(), langCode, sharedPrefManager.getSPCsrfToken()).enqueue(Typescallback);
    }

    /*Lanza las preferencias de la app, pero están deshabilitado el botón ahora*/
    public void LanzarPreferencias(View v) {
        Intent intent = new Intent(this, PreferenciasActivity.class);
        this.startActivity(intent);
    }

    Callback<ArrayList<Node>> Nodecallback = new Callback<ArrayList<Node>>() {
        @Override
        public void onResponse(Call<ArrayList<Node>> call, Response<ArrayList<Node>> response) {
            if (response.isSuccessful()) {
                typesLayout.removeAllViews();
                catalog = new Catalog(response.body());
                catalog.CrearTypes();
                typesAvaliable = catalog.getTypes();
                if (catalog.sincronizarStock(order)) {
                    Toast.makeText(context, getResources().getString(R.string.removed_sync), Toast.LENGTH_SHORT).show();
                }
                LinearLayout fourTypesLayout = new LinearLayout(context);
                fourTypesLayout.setOrientation(LinearLayout.HORIZONTAL);
                fourTypesLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
                fourTypesLayout.setGravity(Gravity.CENTER);
                typesLayout.addView(fourTypesLayout);
                Double imagesHeight = new Float(findViewById(R.id.types_scroll).getHeight()) * 0.40;
                Double titlesHeight = new Float(findViewById(R.id.types_scroll).getHeight()) * 0.10;
                Double typeHeight = new Float(findViewById(R.id.types_scroll).getHeight()) * 0.5;
                LinearLayout.LayoutParams titlesParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        imagesHeight.intValue() + titlesHeight.intValue());
                titlesParams.setMargins(16, 0, 16, 0);

                for (int i = 0; i < typesAvaliable.size(); i++) {
                    final String name = typesAvaliable.get(i);
                    for (Tipo tipo : tipos) {
                        if (tipo.getId().compareTo(name) == 0) {
                            if (i % 4 == 0) {
                                fourTypesLayout = new LinearLayout(context);
                                fourTypesLayout.setOrientation(LinearLayout.HORIZONTAL);
                                fourTypesLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                ));
                                fourTypesLayout.setGravity(Gravity.CENTER);
                                typesLayout.addView(fourTypesLayout);
                            }
                            LinearLayout titleLayout = new LinearLayout(context);
                            titleLayout.setOrientation(LinearLayout.VERTICAL);
                            titleLayout.setLayoutParams(titlesParams);
                            titleLayout.setGravity(Gravity.CENTER);
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
                            try {
                                Picasso.with(context).load(tipo.getUrl()).resize(0, imagesHeight.intValue()).into(image);
                            } catch (Exception e) {
                                Picasso.with(context).load(tipo.getUrl()).into(image);
                                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                            titleLayout.addView(image);
                            TextView text = new TextView(context);
                            text.setHeight(titlesHeight.intValue());
                            text.setText(tipo.getName());
                            text.setGravity(Gravity.CENTER);
                            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(text, 24, 100, 2, TypedValue.COMPLEX_UNIT_SP);
                            titleLayout.addView(text);
                            fourTypesLayout.addView(titleLayout);
                        }
                    }
                }
            }else{
                try {
                    Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
                tipos.clear();
                tipos = response.body();
                mApiService.getAllNodes(sharedPrefManager.getSPStore(), langCode, sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Nodecallback);
            }
        }

        @Override
        public void onFailure(Call<ArrayList<Tipo>> call, Throwable t) {
            Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

}
