
package com.example.plproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray
import org.json.JSONObject

class ReportItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_item)

        // Keep navigation above Android system buttons
        val root = findViewById<LinearLayout>(R.id.reportRoot)

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
            // Already on Report
        }

        findViewById<View>(R.id.navProfile).setOnClickListener {
            openScreen(ProfileActivity::class.java)
        }

        val reportTypeGroup =
            findViewById<RadioGroup>(R.id.reportTypeGroup)

        val itemNameInput =
            findViewById<EditText>(R.id.itemNameInput)

        val locationInput =
            findViewById<EditText>(R.id.locationInput)

        val descriptionInput =
            findViewById<EditText>(R.id.descriptionInput)

        val submitReportButton =
            findViewById<Button>(R.id.submitReportButton)

        val uploadPhotoButton =
            findViewById<Button>(R.id.uploadPhotoButton)

        // Photo upload placeholder
        uploadPhotoButton.setOnClickListener {
            Toast.makeText(
                this,
                "Photo upload will be available soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Submit report
        submitReportButton.setOnClickListener {

            val itemName = itemNameInput.text.toString().trim()
            val location = locationInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()

            val selectedId = reportTypeGroup.checkedRadioButtonId

            if (selectedId == -1) {
                Toast.makeText(
                    this,
                    "Please select Lost or Found",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (itemName.isEmpty()) {
                itemNameInput.error = "Enter item name"
                itemNameInput.requestFocus()
                return@setOnClickListener
            }

            if (location.isEmpty()) {
                locationInput.error = "Enter location"
                locationInput.requestFocus()
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                descriptionInput.error = "Enter description"
                descriptionInput.requestFocus()
                return@setOnClickListener
            }

            val type = when (selectedId) {
                R.id.lostRadio -> "Lost"
                R.id.foundRadio -> "Found"
                else -> {
                    Toast.makeText(
                        this,
                        "Please select a valid report type",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }

            val preferences = getSharedPreferences(
                "CampusFindData",
                MODE_PRIVATE
            )

            val studentEmail =
                preferences.getString("studentEmail", "") ?: ""

            val reports = try {
                JSONArray(
                    preferences.getString("reports", "[]") ?: "[]"
                )
            } catch (e: Exception) {
                JSONArray()
            }

            val report = JSONObject().apply {
                put("type", type)
                put("name", itemName)
                put("location", location)
                put("description", description)
                put("email", studentEmail)
            }

            reports.put(report)

            val saved = preferences.edit()
                .putString("reports", reports.toString())
                .commit()

            if (saved) {

                Toast.makeText(
                    this,
                    "$type item reported successfully!",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(
                    this,
                    MainActivity::class.java
                )

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                )

                startActivity(intent)
                finish()

            } else {

                Toast.makeText(
                    this,
                    "Could not save report. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun openScreen(screen: Class<*>) {
        val intent = Intent(this, screen)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }
}
