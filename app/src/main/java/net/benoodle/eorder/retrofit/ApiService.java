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

    @GET("eorders/types?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Tipo>> getTypes(@Header("Authorization") String user_auth, @Query("langcode") String langcode, @Header("X-CSRF-Token") String x_csrf_token);

    @GET("eorders/products?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Node>> getAllNodes(@Query("store_id") String store_id, @Query("langcode") String langcode, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @POST("/eorders/create?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ResponseBody> addOrder(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body HashMap<String, Object> body);
}