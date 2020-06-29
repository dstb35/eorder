package net.benoodle.eorder.retrofit;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import net.benoodle.eorder.CartActivity;
import net.benoodle.eorder.LoginActivity;
import net.benoodle.eorder.MainActivity;

public class SharedPrefManager {

    public static final String SP_HUBBING_APP = "spHubbingApp";
    public static final String SP_NAME = "spName";
    public static final String SP_EMAIL = "spEmail";
    public static final String SP_PASSWORD = "spPassword";
    public static final String SP_CSRF_TOKEN = "spCsrfToken";
    public static final String SP_LOGOUT_TOKEN = "spLogoutToken";
    public static final String SP_USER_ID = "spUserId";
    public static final String SP_BASIC_AUTH = "spBasicAuth";
    public static final String SP_IS_LOGGED_IN = "spIsLoggedLogin";
    public static final String COOKIE = "spCookie";
    public static final String COOKIE_EXPIRES = "spCookieExpires";
    public static final String URL = "URL";
    public static final String STORE = "store";
    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;

    /*public SharedPrefManager(MainActivity context) {
        sp = context.getSharedPreferences(SP_HUBBING_APP, Context.MODE_PRIVATE);
        spEditor = sp.edit();
    }

    public SharedPrefManager(LoginActivity context) {
        sp = context.getSharedPreferences(SP_HUBBING_APP, Context.MODE_PRIVATE);
        spEditor = sp.edit();
    }

    public SharedPrefManager(CartActivity context) {
        sp = context.getSharedPreferences(SP_HUBBING_APP, Context.MODE_PRIVATE);
        spEditor = sp.edit();
    }*/

    public SharedPrefManager(Context context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        //sp = context.getSharedPreferences(SP_HUBBING_APP, Context.MODE_PRIVATE);
        spEditor = sp.edit();
    }

        public void saveSPString(String keySP, String value) {
        spEditor.putString(keySP, value);
        spEditor.commit();
    }

    public void saveSPInt(String keySP, int value) {
        spEditor.putInt(keySP, value);
        spEditor.commit();
    }

    public void saveSPBoolean(String keySP, boolean value) {
        spEditor.putBoolean(keySP, value);
        spEditor.commit();
    }

    public String getSPName() {
        return sp.getString(SP_NAME, "");
    }
    public String getSPPassword() {
        return sp.getString(SP_PASSWORD, "");
    }
    public String getSPEmail() {
        return sp.getString(SP_EMAIL, "");
    }
    public Boolean getSPIsLoggedIn() {
        return sp.getBoolean(SP_IS_LOGGED_IN, false);
    }
    public String getSPCsrfToken() {
        return sp.getString(SP_CSRF_TOKEN, "");
    }
    public String getSPCsrfLogoutToken() {
        return sp.getString(SP_LOGOUT_TOKEN, "");
    }
    public String getSPUserId() {
        return sp.getString(SP_USER_ID, "");
    }
    public String getSPBasicAuth() {
        return sp.getString(SP_BASIC_AUTH, "");
    }
    public String getSPCookie() {
        return sp.getString(COOKIE, "");
    }
    public String getSPStore() {
        return sp.getString(STORE, "");
    }
    public String getSPUrl() {
        return sp.getString(URL, "");
    }
    public String getSPCookieExpires() { return sp.getString(COOKIE_EXPIRES, ""); }

}
