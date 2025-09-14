// app/src/main/java/com/harmoni/MainActivity.kt
package com.harmoni

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnScanMedia: Button
    private lateinit var textStatus: TextView

    // Permission request code
    companion object {
        private const val REQUEST_READ_STORAGE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupClickListeners()
        updateStatus("Ready to scan your media library")
    }

    private fun bindViews() {
        btnScanMedia = findViewById(R.id.btn_scan_media)
        textStatus = findViewById(R.id.text_status)
    }

    private fun setupClickListeners() {
        btnScanMedia.setOnClickListener {
            requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted
                onPermissionGranted()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                // User previously denied — show explanation
                showPermissionRationale()
            }

            else -> {
                // Request permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_STORAGE
                )
            }
        }
    }

    private fun showPermissionRationale() {
        Toast.makeText(
            this,
            "Harmoni needs access to storage to play your videos and music.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_READ_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted()
            } else {
                updateStatus("❌ Storage access denied")
                Toast.makeText(this, "Cannot access media without permission.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onPermissionGranted() {
        updateStatus("✅ Storage access granted!")
        Toast.makeText(this, "Now scanning your media...", Toast.LENGTH_SHORT).show()
        // We'll add actual scanning logic next!
    }

    private fun updateStatus(text: String) {
        textStatus.text = text
    }
}