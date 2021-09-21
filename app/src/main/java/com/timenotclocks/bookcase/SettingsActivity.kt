package com.timenotclocks.bookcase

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*


class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, SettingsFragment())
                    .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val source: Preference? = findPreference("source")
            source?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val url = "https://github.com/fenimore/badreads"
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(url)
                startActivity(i)
                true
            }
            val version: Preference? = findPreference("version")
            version?.summary = BuildConfig.VERSION_NAME
            val dark: ListPreference? = findPreference("dark_mode")
            dark?.summary = dark?.entry
            val tab: ListPreference? = findPreference("landing_tab")
            tab?.summary = tab?.entry
            tab?.setOnPreferenceChangeListener { preference, newValue ->
                val tabArray = resources.getStringArray(R.array.tab_array)
                preference.summary = "${tabArray.get(newValue.toString().toInt())}"
                true
            }

            val yearlyGoal: EditTextPreference? = findPreference("yearly_goal")
            yearlyGoal?.setOnBindEditTextListener { edit ->
                edit.inputType = InputType.TYPE_CLASS_NUMBER
            }
            yearlyGoal?.summary = "Goal to read this year: ${yearlyGoal?.text}"
            yearlyGoal?.setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = "Goal to read this year: ${newValue}"
                true
            }


            val remoteUrl: EditTextPreference? = findPreference("remote_url")
            remoteUrl?.setOnBindEditTextListener { edit ->
                edit.inputType = InputType.TYPE_TEXT_VARIATION_URI
            }
            remoteUrl?.summary = "API URL: ${remoteUrl?.text}"
            remoteUrl?.setOnPreferenceChangeListener { preference, newValue ->
                preference.summary = "API URL: ${newValue}"
                true
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == null || sharedPreferences == null) return
        val darkModeString = getString(R.string.dark_mode_prerence)
        when (key) {
            "landing_tab" -> {}
            "yearly_goal" -> {}
            "remote_url" -> {}
            darkModeString -> {
                val darkModeValues = resources.getStringArray(R.array.dark_mode_values)
                when (sharedPreferences.getString(darkModeString, darkModeValues[0])) {
                    darkModeValues[0] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    darkModeValues[1] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    darkModeValues[2] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    darkModeValues[3] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                }
            }
        }
    }
}