package net.benoodle.eorder;

import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static android.view.View.GONE;
import static net.benoodle.eorder.MainActivity.catalog;
import static net.benoodle.eorder.MainActivity.order;

import net.benoodle.eorder.model.Catalog;
import net.benoodle.eorder.model.Node;
import net.benoodle.eorder.model.Order;
import net.benoodle.eorder.model.OrderItem;
import net.benoodle.eorder.model.User;
import net.benoodle.eorder.retrofit.ApiService;
import net.benoodle.eorder.retrofit.SharedPrefManager;
import net.benoodle.eorder.retrofit.UtilsApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.EliminarListener {

    private CartAdapter adaptador;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    SharedPrefManager sharedPrefManager;
    ApiService mApiService;
    private HashMap<String, Object> body = new HashMap<>();
    private User user;
    private TextView Btotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(CartActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        mApiService = UtilsApi.getAPIService();
        sharedPrefManager = new SharedPrefManager(this);
        this.Btotal = findViewById(R.id.Btotal);
        if (!sharedPrefManager.getSPIsLoggedIn()) {
            Intent loginIntent = new Intent(CartActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }

    protected void onStart(){
        super.onStart();
        if (!ComprobarProductos()){
            AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
            builder.setTitle(R.string.removed);
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
        }
        CambiarAdaptador();
        ActualizarTotal();
        if (order.getOrderItems().size() == 0){
            Toast.makeText(getApplicationContext(), R.string.cart_empty, Toast.LENGTH_LONG).show();
            findViewById(R.id.Btcomprar).setVisibility(GONE);
        }
    }

    public boolean ComprobarProductos () {
        /*Antes de mostrar el carrito hay que comprobar que no se haya despublicado
        algún producto que ya estuviera en el carrito anteriormente.
        Devuelve true si no ha eliminado nada, false si hubo que borrar algo.*/
        boolean result = true;
        ArrayList<OrderItem> removeOrderItems = new ArrayList<>();
        for (OrderItem orderItem : order.getOrderItems()){
           if (!catalog.CheckNodebySku(orderItem.getSku())){
               removeOrderItems.add(orderItem);
               result = false;
               Toast.makeText(getApplicationContext(),  getResources().getString(R.string.no_stock)+orderItem.getTitle(), Toast.LENGTH_SHORT).show();
               continue;
           }
           //Miramos las selecciones de los menus
           if (orderItem.getSelecciones().size() > 0 ){
               for (String seleccion : orderItem.getSelecciones()){
                   if (!catalog.CheckNodebyTitle(seleccion)) {
                       removeOrderItems.add(orderItem);
                       result = false;
                       Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_stock)+orderItem.getTitle(), Toast.LENGTH_SHORT).show();
                       break;
                   }
               }
           }

        }
        order.removeAllOrderItems(removeOrderItems);
        return result;
    }

    public void CambiarAdaptador (){
        //Se supone que con adaptador.notifyDataSetChange() debería ir pero no funciona
        if (order.getOrderItems().isEmpty()){
            finish();
        }
        adaptador = new CartAdapter( CartActivity.this, this);
        recyclerView.setAdapter(adaptador);
        //recyclerView.getAdapter().notifyDataSetChanged();
    }

    /*Método asociado al botón Comprar*/
    public void Comprar(View v) {
        order.setEmail(sharedPrefManager.getSPEmail());
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
                    builder.setTitle(String.format("%s%s", getResources().getString(R.string.ordercompleted), order.getOrderId()));
                    builder.setCancelable(false);
                    builder.setPositiveButton(R.string.gotIt, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            order = new Order();
                            dialog.dismiss();
                            finish();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }else{
                    /*TODO
                    Poner códigos de error en ENDPOINT
                     */
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
        order.removeOrderItem(i);
        if (order.getOrderItems().isEmpty()){
            finish();
        }
    }

    @Override
    public void ActualizarTotal(){
        try {
            Btotal.setText("");
            Btotal.setText(String.format("%s%s €", getResources().getString(R.string.total), String.format("%.2f", order.getTotal())));
            CambiarAdaptador();
        }catch (Exception e) {
            Btotal.setText(e.getLocalizedMessage());
        }
    }
}
