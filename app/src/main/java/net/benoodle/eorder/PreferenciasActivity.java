package net.benoodle.eorder;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import net.benoodle.eorder.R;
import androidx.appcompat.app.AppCompatActivity;

public class PreferenciasActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_preferencias);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_preferencias, new PreferenciasFragment())
                .commit();
    }

    public void doDone(View v){
        finish();
    }

}
