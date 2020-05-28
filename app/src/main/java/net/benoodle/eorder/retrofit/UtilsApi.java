package net.benoodle.eorder.retrofit;

public class UtilsApi {

    // Add your API url here.
    public static final String BASE_URL_API = "https://benoodle.net";

    public static ApiService getAPIService() {
        return RetrofitInstance.getRetrofitInstance(BASE_URL_API).create(ApiService.class);
    }
}
