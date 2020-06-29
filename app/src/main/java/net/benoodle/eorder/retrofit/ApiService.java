package net.benoodle.eorder.retrofit;

import net.benoodle.eorder.model.LoginData;
import net.benoodle.eorder.model.Node;
import net.benoodle.eorder.model.Tipo;
import java.util.ArrayList;
import java.util.HashMap;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("user/login?_format=json")
    @Headers({"Content-type: application/x-www-form-urlencoded"})
    Call<ResponseBody> loginRequest(@Body LoginData body);

    @GET("node/{node_id}?_format=json")
    @Headers({"Content-type: application/json"})
    Call<Node> getNode(@Header("Authorization") String user_auth, @Path("node_id") String node_id);

    @GET("eorders/types?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Tipo>> getTypes(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    /*@GET("eorder/?_format=json")
    @Headers({"Content-type: application/json"})
    Call<NodeList<Node>> getAllNodes(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);*/

    /*@GET("eorder/?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Node>> getAllNodes(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);*/

    @GET("eorders/products?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Node>> getAllNodes(@Query("store_id") String store_id, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    /*@GET("eorder/?_format=json")
    @Headers({"Content-type: application/json"})
    Call<List<Node>> getAllNodes(@Header("Cookie") OR "Set-Cookie" String cookie, @Header("X-CSRF-Token") String x_csrf_token);*/

    @POST("/node?_format=json")
    Call<Node> addNode(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body Node node);

    //@POST("/commerce/order/create")
    @POST("/eorders/create?_format=json")
    @Headers({"Content-type: application/json"})
    //Call<ResponseBody> addOrder(@Header("Authorization") String user_auth, @Header("Commerce-Cart-Token") String x_csrf_token, @Body Order order);
    //Call<ResponseBody> addOrder(@Header("Authorization") String user_auth, @Header("Commerce-Cart-Token") String x_csrf_token, @Body String order);
    //Call<ResponseBody> addOrder(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body Object[] body);
    //Call<ArrayList<Cart>> addOrder(@Header("Authorization") String user_auth, @Header("Commerce-Cart-Token") String x_csrf_token, @Body Object[] body);
    //Call<ResponseBody> addOrder(@Header("Authorization") String user_auth, @Header("Commerce-Cart-Token") String x_csrf_token, @Body Object[] body);
    Call<ResponseBody> addOrder(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body HashMap<String, Object> body);
}