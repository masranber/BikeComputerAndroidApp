package com.masranber.bikecomputer;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        Preference versionInfo = findPreference("version");
        try {
            versionInfo.setSummary(getVersionBuildString());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getVersionBuildString() throws PackageManager.NameNotFoundException {
        PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
        return pInfo.versionName + " ("+pInfo.versionCode+")";
    }
}