
package com.example.plproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProfileActivity : AppCompatActivity() {

    private lateinit var profileHeaderName: TextView
    private lateinit var profileName: TextView
    private lateinit var profileCollege: TextView
    private lateinit var profileEmail: TextView

    private lateinit var preferences: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        preferences = getSharedPreferences(
            "CampusFindData",
            MODE_PRIVATE
        )

        // Set default account information if details are missing
        setDefaultProfile()

        // Initialize profile views
        profileHeaderName = findViewById(R.id.profileHeaderName)
        profileName = findViewById(R.id.profileName)
        profileCollege = findViewById(R.id.profileCollege)
        profileEmail = findViewById(R.id.profileEmail)

        // Adjust screen for Android system navigation
        val root = findViewById<LinearLayout>(R.id.profileRoot)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->

            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                bars.left,
                bars.top,
                bars.right,
                bars.bottom
            )

            insets
        }

        ViewCompat.requestApplyInsets(root)

        // Bottom navigation
        findViewById<View>(R.id.navHome).setOnClickListener {
            openScreen(MainActivity::class.java)
        }

        findViewById<View>(R.id.navBrowse).setOnClickListener {
            openScreen(BrowseItemsActivity::class.java)
        }

        findViewById<View>(R.id.navReport).setOnClickListener {
            openScreen(ReportItemActivity::class.java)
        }

        findViewById<View>(R.id.navProfile).setOnClickListener {
            // Already on Profile
        }

        // My Reports removed

        // Settings
        findViewById<TextView>(R.id.settingsButton)
            .setOnClickListener {
                Toast.makeText(
                    this,
                    "Settings coming soon!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        // Help and Support
        findViewById<TextView>(R.id.helpButton)
            .setOnClickListener {
                Toast.makeText(
                    this,
                    "Contact CampusFind support.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        loadProfile()
    }

    private fun setDefaultProfile() {

        val currentName = preferences.getString("studentName", "")
        val currentCollege = preferences.getString("studentCollege", "")
        val currentEmail = preferences.getString("studentEmail", "")

        val editor = preferences.edit()

        if (currentName.isNullOrBlank() ||
            currentName == "Student Name"
        ) {
            editor.putString("studentName", "Raviraj Kadam")
        }

        if (currentCollege.isNullOrBlank() ||
            currentCollege == "College not provided"
        ) {
            editor.putString(
                "studentCollege",
                "DYPSEM College, Kolhapur"
            )
        }

        if (currentEmail.isNullOrBlank() ||
            currentEmail == "Email not provided"
        ) {
            editor.putString(
                "studentEmail",
                "kadamraviraj3010@gmail.com"
            )
        }

        editor.apply()
    }

    private fun loadProfile() {

        val name = preferences.getString(
            "studentName",
            "Raviraj Kadam"
        ) ?: "Raviraj Kadam"

        val college = preferences.getString(
            "studentCollege",
            "DYPSEM College, Kolhapur"
        ) ?: "DYPSEM College, Kolhapur"

        val email = preferences.getString(
            "studentEmail",
            "kadamraviraj3010@gmail.com"
        ) ?: "kadamraviraj3010@gmail.com"

        profileHeaderName.text = name
        profileName.text = name
        profileCollege.text = college
        profileEmail.text = email
    }

    override fun onResume() {
        super.onResume()

        if (::preferences.isInitialized &&
            ::profileName.isInitialized
        ) {
            loadProfile()
        }
    }

    private fun openScreen(screen: Class<*>) {

        val intent = Intent(this, screen)

        intent.addFlags(
            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        )

        startActivity(intent)
    }
}
