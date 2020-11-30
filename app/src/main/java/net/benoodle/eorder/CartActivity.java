package net.benoodle.eorder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import static android.view.View.GONE;
import static net.benoodle.eorder.TypesActivity.catalog;
import static net.benoodle.eorder.TypesActivity.order;
import net.benoodle.eorder.model.Order;
import net.benoodle.eorder.model.User;
import net.benoodle.eorder.retrofit.ApiService;
import net.benoodle.eorder.retrofit.SharedPrefManager;
import net.benoodle.eorder.retrofit.UtilsApi;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.EliminarListener {

    private CartAdapter adaptador;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private SharedPrefManager sharedPrefManager;
    private ApiService mApiService;
    private HashMap<String, Object> body = new HashMap<>();
    private User user;
    private TextView Btotal;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_cart);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(CartActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        sharedPrefManager = new SharedPrefManager(this);
        mApiService = UtilsApi.getAPIService(sharedPrefManager.getSPUrl());
        this.Btotal = findViewById(R.id.Btotal);
        if (!sharedPrefManager.getSPIsLoggedIn()) {
            Intent loginIntent = new Intent(CartActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }

    protected void onStart(){
        super.onStart();
        CambiarAdaptador();
        ActualizarTotal();
        if (order.getOrderItems().size() == 0){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.cart_empty), Toast.LENGTH_LONG).show();
            findViewById(R.id.Btcomprar).setVisibility(GONE);
        }
        this.context = getApplicationContext();
    }

    public void CambiarAdaptador (){
        if (order.getOrderItems().isEmpty()){
            finish();
        }
        adaptador = new CartAdapter( CartActivity.this, this);
        recyclerView.setAdapter(adaptador);
    }

    /*Método asociado al botón Comprar*/
    public void Comprar(View v) {
        order.setEmail(sharedPrefManager.getSPEmail());
        Map<String, Integer> totalQuantity = order.calculateTotalsQuantity();
        body.put("totalQuantity", totalQuantity);
        body.put("order", order);
        user = new User(sharedPrefManager.getSPEmail(), sharedPrefManager.getSPName());
        body.put("user", user);
        mApiService.addOrder(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Ordercallback);
    }

    /*Método asociado al botón Catalog para volver a la página de productos, acitivityMain*/
    public void Catalog(View v){
        finish();
    }

    Callback<ResponseBody> Ordercallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody>response) {
            if (response.isSuccessful()) {
                try {
                    JSONObject jsonRESULTS = new JSONObject(response.body().string());
                    String id = jsonRESULTS.getJSONArray("order_id").getJSONObject(0).getString("value");
                    order.setOrderId(id);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                if (sharedPrefManager.getSPModus()){
                    builder.setTitle(String.format("%s%s", getResources().getString(R.string.ordermoduson), order.getOrderId()));
                }else{
                    builder.setTitle(String.format("%s%s", getResources().getString(R.string.ordermodusoff), order.getOrderId()));
                }
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.gotIt, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        order = new Order(sharedPrefManager.getSPStore());
                        dialog.dismiss();
                        setResult(RESULT_OK);
                        finish();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }else if (response.code() == 409 ){
                //En el server cuando algún producto haya sido despublicado durante una compra
                //se lanza el error 409 con los id's que no se pueden comprar para quitarlos del carrito
                try {
                    JSONArray jObjError = new JSONArray(response.errorBody().string());
                    for (int i=0; i < jObjError.length(); i++) {
                        JSONObject node = (JSONObject) jObjError.get(i);
                        catalog.actualizarStock(node.get("id").toString(), node.get("stock").toString());
                        order.removeOrderItemByStock(node.get("id").toString());
                    }
                    ActualizarTotal();
                    adaptador.notifyDataSetChanged();
                    AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                    TextView textView = new TextView(context);
                    textView.setText(getResources().getString(R.string.removed));
                    builder.setCustomTitle(textView);
                    builder.setCancelable(false);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } catch (Exception e){
                    e.getLocalizedMessage();
                }
            }else{
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
        }
    };

    @Override
    public void Eliminar(int i) {
        try{
            order.removeOrderItem(i);
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT);
        }

        if (order.getOrderItems().isEmpty()){
            finish();
        }
    }

    @Override
    public void ActualizarTotal(){
        try {
            Btotal.setText("");
            Btotal.setText(String.format("%s%s €", getResources().getString(R.string.total), String.format("%.2f", order.getTotal())));
            //CambiarAdaptador();
        } catch (Exception e) {
            Btotal.setText(e.getLocalizedMessage());
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void Añadir(String productID, int quantity, Boolean menu, int i){
        try{
            if (menu && quantity > 0) {
                Intent intent = new Intent(this, MenuActivity.class);
                intent.putExtra("id", productID);
                this.startActivity(intent);
            }else if (!menu){
                order.addOrderItem(productID, quantity);
            }else if (menu && quantity < 0){
                this.Eliminar(i);
            }
        }catch (Exception e){
            Toast.makeText(this, getResources().getString(R.string.no_sell), Toast.LENGTH_SHORT).show();
        }
        //Podría ocurrir que con el botón menos lleguemos a no tener orderItems en el carrito
        if (order.getOrderItems().isEmpty()){
            finish();
        }
    }
}
