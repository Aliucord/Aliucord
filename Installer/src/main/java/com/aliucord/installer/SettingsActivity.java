package com.aliucord.installer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setTitle("Settings");
        getSupportFragmentManager().beginTransaction().add(R.id.settings_layout, new SettingsFragment()).commit();
    }
}
