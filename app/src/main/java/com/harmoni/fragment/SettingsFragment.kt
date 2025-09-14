// app/src/main/java/com/harmoni/fragment/SettingsFragment.kt
package com.harmoni.fragment

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fragment_settings, rootKey)

        // Find preferences
        val darkModePref = findPreference<SwitchPreferenceCompat>("dark_mode")
        val equalizerPref = findPreference<SwitchPreferenceCompat>("enable_equalizer")
        val sendAnalyticsPref = findPreference<SwitchPreferenceCompat>("send_analytics")
        val backupPlaylistsPref = findPreference<Preference>("backup_playlists")
        val restorePlaylistsPref = findPreference<Preference>("restore_playlists")
        val openSourceLicensesPref = findPreference<Preference>("open_source_licenses")
        val versionPref = findPreference<Preference>("version")

        // Dark Mode
        darkModePref?.setOnPreferenceChangeListener { _, newValue ->
            val useDark = newValue as? Boolean ?: false
            val mode = if (useDark) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            AppCompatDelegate.setDefaultNightMode(mode)
            Toast.makeText(context, "Theme will apply on restart", Toast.LENGTH_SHORT).show()
            true
        }

        // Equalizer Toggle (Info Only)
        equalizerPref?.setOnPreferenceClickListener {
            Toast.makeText(context, "Equalizer settings in the Equalizer tab", Toast.LENGTH_LONG).show()
            true
        }

        // Analytics Consent
        sendAnalyticsPref?.setOnPreferenceChangeListener { preference, newValue ->
            val accepted = newValue as? Boolean ?: false
            if (accepted) {
                Toast.makeText(context, "Thank you for helping improve Harmoni!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Analytics disabled", Toast.LENGTH_SHORT).show()
            }
            // In real app: initialize Firebase or Sentry based on this
            true
        }

        // Backup Playlists
        backupPlaylistsPref?.setOnPreferenceClickListener {
            // TODO: Implement export logic
            Toast.makeText(context, "Backup not yet implemented", Toast.LENGTH_SHORT).show()
            // In future: call BackupRestoreManager.exportPlaylists(requireContext())
            true
        }

        // Restore Playlists
        restorePlaylistsPref?.setOnPreferenceClickListener {
            // TODO: Implement import logic
            Toast.makeText(context, "Restore not yet implemented", Toast.LENGTH_SHORT).show()
            // Use Intent to pick file from storage
            true
        }

        // Open Source Licenses
        openSourceLicensesPref?.setOnPreferenceClickListener {
            try {
                findPreference<Preference>("open_source_licenses")?.summary = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
                    .filter { it.applicationInfo.metaData?.keySet()?.contains("com.google.android.gms.version") == true }
                    .joinToString(", ") { it.applicationInfo.loadLabel(packageManager).toString() }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Toast.makeText(context, "Open source libraries used in Harmoni", Toast.LENGTH_LONG).show()
            true
        }

        // Version Click
        versionPref?.setOnPreferenceClickListener {
            Toast.makeText(context, "Harmoni v1.0 • Built with ❤️", Toast.LENGTH_LONG).show()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.title = "Settings"
    }
}