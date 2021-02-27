package com.aliucord.installer;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs);

        Preference dexLocation = findPreference("dex_location");
        dexLocation.setSummary(getPreferenceManager().getSharedPreferences().getString("dex_location", MainActivity.DEFAULT_DEX_LOCATION));
        dexLocation.setOnPreferenceClickListener(p -> {
            new MaterialFilePicker()
                    .withSupportFragment(this)
                    .withCloseMenu(true)
                    .withHiddenFiles(true)
                    .withFilter(Pattern.compile(".*\\.dex$"))
                    .withFilterDirectories(false)
                    .withTitle("Select Aliucord.dex")
                    .withRequestCode(1)
                    .start();
            return true;
        });
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null || requestCode != 1) return;
        findPreference("dex_location").setSummary(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
        getPreferenceManager().getSharedPreferences().edit().putString("dex_location", data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH)).apply();
    }
}
