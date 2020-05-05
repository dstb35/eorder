package net.benoodle.eorder;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;


import net.benoodle.eorder.R;

public class PreferenciasFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey){
        //super.onCreate(savedInstanceState);
        setPreferencesFromResource(R.xml.preferencias, rootKey);
        //addPreferencesFromResource(R.xml.preferencias);
    }
}
