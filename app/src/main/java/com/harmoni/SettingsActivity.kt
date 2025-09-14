// app/src/main/java/com/harmoni/SettingsActivity.kt
package com.harmoni

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, com.harmoni.fragment.SettingsFragment())
                .commit()
        }

        // Back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Settings"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}