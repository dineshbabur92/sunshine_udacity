package com.learningapp1992.dineshbaburengasamy.sunshine;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
//remove - classrem001
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        // TODO: Add preferences from XML
        addPreferencesFromResource(R.xml.pref_general);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        // TODO: Add preferences
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        //bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));

    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference locationPref = findPreference(getString(R.string.location_key    ));
        //Preference unitsPref = findPreference(getString(R.string.));
        prefChanged(sharedPreferences, locationPref, key);
        //prefChanged(sharedPreferences, unitsPref, key);
    }

    private void prefChanged(SharedPreferences sharedPreferences, Preference pref, String key) {
        if (sharedPreferences instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) sharedPreferences;
            int prefIndex = listPreference.findIndexOfValue(key);
            if (prefIndex >= 0) {
                pref.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            pref.setSummary(sharedPreferences.getString(key, ""));
        }
    }

}
