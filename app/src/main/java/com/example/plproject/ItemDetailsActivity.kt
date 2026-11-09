
package com.example.plproject

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class ItemDetailsActivity : AppCompatActivity() {

    private var reportIndex = -1
    private var reportType = ""
    private var reporterEmail = ""

    private lateinit var report: JSONObject
    private lateinit var statusText: TextView
    private lateinit var itemNameText: TextView
    private lateinit var itemDescriptionText: TextView
    private lateinit var locationText: TextView
    private lateinit var reportedByText: TextView
    private lateinit var contactDetailsText: TextView
    private lateinit var resolveButton: Button
    private lateinit var contactButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details)

        statusText = findViewById(R.id.detailStatus)
        itemNameText = findViewById(R.id.detailItemName)
        itemDescriptionText = findViewById(R.id.detailDescription)
        locationText = findViewById(R.id.detailLocation)
        reportedByText = findViewById(R.id.reportedByText)
        contactDetailsText = findViewById(R.id.contactDetailsText)
        resolveButton = findViewById(R.id.resolveButton)
        contactButton = findViewById(R.id.contactButton)

        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }

        loadReport()

        resolveButton.setOnClickListener {
            showResolveConfirmation()
        }

        contactButton.setOnClickListener {
            contactReporter()
        }
    }

    private fun loadReport() {

        val preferences = getSharedPreferences(
            "CampusFindData", MODE_PRIVATE
        )

        val reports = try {
            JSONArray(preferences.getString("reports", "[]") ?: "[]")
        } catch (e: Exception) {
            JSONArray()
        }

        reportIndex = intent.getIntExtra("reportIndex", -1)

        if (reportIndex !in 0 until reports.length()) {

            val type = intent.getStringExtra("type") ?: ""
            val name = intent.getStringExtra("name") ?: ""
            val location = intent.getStringExtra("location") ?: ""
            val description = intent.getStringExtra("description") ?: ""

            for (i in reports.length() - 1 downTo 0) {
                val item = reports.optJSONObject(i) ?: continue

                if (
                    item.optString("type") == type &&
                    item.optString("name") == name &&
                    item.optString("location") == location &&
                    item.optString("description") == description
                ) {
                    reportIndex = i
                    break
                }
            }
        }

        if (reportIndex !in 0 until reports.length()) {
            Toast.makeText(
                this,
                "Report not found.",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        report = reports.optJSONObject(reportIndex) ?: JSONObject()

        reportType = report.optString("type", "")
        reporterEmail = report.optString("email", "")

        displayReport()
    }

    private fun displayReport() {

        val isResolved = report.optString(
            "status", "Active"
        ).equals("Resolved", true)

        itemNameText.text = report.optString("name", "Unnamed Item")
        itemDescriptionText.text =
            report.optString("description", "No description provided.")

        locationText.text =
            report.optString("location", "Location not provided")

        if (isResolved) {
            statusText.text = "RESOLVED"
            statusText.setTextColor(0xFF047857.toInt())
            statusText.setBackgroundColor(0xFFD1FAE5.toInt())

            resolveButton.visibility = View.GONE

        } else {

            if (reportType.equals("Lost", true)) {
                statusText.text = "LOST ITEM"
                statusText.setTextColor(0xFFB91C1C.toInt())
                statusText.setBackgroundColor(0xFFFEE2E2.toInt())

                resolveButton.text = "I Found This Item"

            } else {
                statusText.text = "FOUND ITEM"
                statusText.setTextColor(0xFF047857.toInt())
                statusText.setBackgroundColor(0xFFD1FAE5.toInt())

                resolveButton.text = "Item Returned to Owner"
            }

            // Show the action to everyone viewing an active report.
            resolveButton.visibility = View.VISIBLE
        }

        reportedByText.text = "Reported by: CampusFind Student"

        contactDetailsText.text =
            if (reporterEmail.isNotEmpty()) {
                "Contact details: $reporterEmail"
            } else {
                "Contact details: Not provided"
            }

        contactButton.text =
            if (reportType.equals("Lost", true)) {
                "Contact Owner"
            } else {
                "Contact Finder"
            }
    }

    private fun showResolveConfirmation() {

        val message = if (reportType.equals("Lost", true)) {
            "Have you found this lost item? Confirm to move this report to Resolved Reports."
        } else {
            "Has this found item been returned to its owner? Confirm to resolve this report."
        }

        AlertDialog.Builder(this)
            .setTitle("Resolve Report?")
            .setMessage(message)
            .setPositiveButton("Yes, Resolve") { _, _ ->
                markReportResolved()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun markReportResolved() {

        val preferences = getSharedPreferences(
            "CampusFindData", MODE_PRIVATE
        )

        val reports = try {
            JSONArray(preferences.getString("reports", "[]") ?: "[]")
        } catch (e: Exception) {
            JSONArray()
        }

        if (reportIndex !in 0 until reports.length()) {
            Toast.makeText(
                this, "Report not found.", Toast.LENGTH_SHORT
            ).show()
            return
        }

        val savedReport = reports.optJSONObject(reportIndex)

        if (savedReport == null) {
            Toast.makeText(
                this, "Report not found.", Toast.LENGTH_SHORT
            ).show()
            return
        }

        savedReport.put("status", "Resolved")

        val saved = preferences.edit()
            .putString("reports", reports.toString())
            .commit()

        if (saved) {
            report = savedReport

            Toast.makeText(
                this,
                "Report successfully resolved!",
                Toast.LENGTH_LONG
            ).show()

            displayReport()

        } else {
            Toast.makeText(
                this,
                "Could not update report.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun contactReporter() {

        if (reporterEmail.isEmpty()) {
            Toast.makeText(
                this,
                "Contact email is not available.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val emailIntent = android.content.Intent(
            android.content.Intent.ACTION_SENDTO,
            Uri.parse("mailto:$reporterEmail")
        )

        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            Toast.makeText(
                this,
                "No email application found.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
