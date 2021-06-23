package net.benoodle.eorder;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import net.benoodle.eorder.model.*;
import net.benoodle.eorder.retrofit.*;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView, mUrl;
    private View mProgressView;
    private SharedPrefManager sharedPrefManager;
    private ApiService mApiService;
    private Button mEmailSignInButton;
    private String email, password, URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPrefManager = new SharedPrefManager(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mUsernameView = findViewById(R.id.username);
        mUsernameView.setText(sharedPrefManager.getSPEmail());
        mUsernameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == EditorInfo.IME_ACTION_NEXT && v.getId() == mUsernameView.getId()) {
                    mPasswordView.requestFocus();
                }
                return true;
            }
        });
        mPasswordView = findViewById(R.id.password);
        mPasswordView.setText(sharedPrefManager.getSPPassword());
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == EditorInfo.IME_ACTION_DONE && v.getId() == mPasswordView.getId()) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(getApplication().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    takeCredentials();
                }
                return true;
            }
        });
        mUrl = findViewById(R.id.url);
        mUrl.setText(sharedPrefManager.getSPUrl());
        mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                takeCredentials();
            }
        });
        //chvoluntarios = findViewById(R.id.chvoluntarios);
        mProgressView = findViewById(R.id.login_progress);
    }

    public void takeCredentials() {
        email = mUsernameView.getText().toString().trim();
        password = mPasswordView.getText().toString().trim();
        URL = mUrl.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.empty_login), Toast.LENGTH_SHORT).show();
        } else if (URL.isEmpty()) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.empty_url), Toast.LENGTH_SHORT).show();
        } else {
            try {
                mProgressView.setVisibility(View.VISIBLE);
                attemptLogin();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void attemptLogin() {
        mApiService = UtilsApi.getAPIService(URL);
        mApiService.loginRequest(new LoginData(email, password, Locale.getDefault().getLanguage()))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        mProgressView.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                if (!jsonRESULTS.getString("csrf_token").isEmpty()) {
                                    String[] Cookies = response.headers().get("Set-Cookie").split(";", 4);
                                    String csrf_token = jsonRESULTS.getString("csrf_token");
                                    String logout_token = jsonRESULTS.getString("logout_token");
                                    String user_id = jsonRESULTS.getJSONObject("current_user").getString("uid");
                                    String name = jsonRESULTS.getJSONObject("current_user").getString("name");
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_NAME, name);
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_PASSWORD, password);
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_EMAIL, email);
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_CSRF_TOKEN, csrf_token);
                                    sharedPrefManager.saveSPString(SharedPrefManager.URL, URL);
                                    //sharedPrefManager.saveSPString(SharedPrefManager.STORE, store_id);
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_LOGOUT_TOKEN, logout_token);
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_USER_ID, user_id);
                                    sharedPrefManager.saveSPBoolean(SharedPrefManager.SP_IS_LOGGED_IN, true);
                                    sharedPrefManager.saveSPString(SharedPrefManager.COOKIE, Cookies[0]);
                                    sharedPrefManager.saveSPString(SharedPrefManager.COOKIE_EXPIRES, Cookies[1]);
                                    //sharedPrefManager.saveSPBoolean(SharedPrefManager.VOLUNTARIOS, chvoluntarios.isChecked());
                                    //String basic_auth = username + ":" + password;
                                    String basic_auth = name + ":" + password;
                                    byte[] bytes_basic_auth = basic_auth.getBytes();
                                    String encoded_basic_auth = android.util.Base64.encodeToString(bytes_basic_auth, android.util.Base64.DEFAULT);
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_BASIC_AUTH, "Basic " + encoded_basic_auth.trim());
                                    //Intent intent = new Intent(LoginActivity.this, TypesActivity.class);
                                    Intent intent = new Intent(LoginActivity.this, StoresActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    String error_message = jsonRESULTS.getString("error_msg");
                                    Toast.makeText(LoginActivity.this, error_message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.errorBody().string());
                                String error_message = jsonRESULTS.getString("message");
                                Toast.makeText(LoginActivity.this, error_message, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        mProgressView.setVisibility(View.GONE);
                    }
                });
    }
}