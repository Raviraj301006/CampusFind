package com.example.plproject

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class MyReportsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_report)
    }

    override fun onResume() {
        super.onResume()
        displayMyReports()
    }

    private fun displayMyReports() {
        val container = findViewById<LinearLayout>(R.id.myReportsContainer)
        val emptyText = findViewById<TextView>(R.id.emptyReportsText)

        container.removeAllViews()

        val preferences = getSharedPreferences("CampusFindData", MODE_PRIVATE)
        val currentEmail = preferences.getString("studentEmail", "") ?: ""

        val reports = try {
            JSONArray(preferences.getString("reports", "[]") ?: "[]")
        } catch (e: Exception) {
            JSONArray()
        }

        var count = 0

        for (i in reports.length() - 1 downTo 0) {
            val report = reports.optJSONObject(i) ?: continue
            val reportEmail = report.optString("email", "")

            if (currentEmail.isBlank() ||
                reportEmail.isBlank() ||
                !reportEmail.equals(currentEmail, ignoreCase = true)
            ) {
                continue
            }

            addReportCard(container, report)
            count++
        }

        emptyText.visibility = if (count == 0) TextView.VISIBLE else TextView.GONE
    }

    private fun addReportCard(container: LinearLayout, report: JSONObject) {
        val type = report.optString("type", "Item")
        val name = report.optString("name", "Unnamed item")
        val location = report.optString("location", "Location not provided")
        val description = report.optString("description", "No description provided")

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 16, 18, 16)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 20f
                setStroke(1, Color.rgb(226, 232, 240))
            }
        }

        val cardParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParams.bottomMargin = 16

        val typeText = TextView(this).apply {
            text = "$type ITEM"
            textSize = 12f
            setTextColor(
                if (type.equals("Lost", true)) {
                    Color.rgb(185, 28, 28)
                } else {
                    Color.rgb(21, 128, 61)
                }
            )
            setTypeface(null, Typeface.BOLD)
        }

        val nameText = TextView(this).apply {
            text = name
            textSize = 19f
            setTextColor(Color.rgb(23, 37, 84))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 8, 0, 6)
        }

        val locationText = TextView(this).apply {
            text = "Location: $location"
            textSize = 14f
            setTextColor(Color.rgb(71, 85, 105))
        }

        val detailsButton = Button(this).apply {
            text = "View Details"
            isAllCaps = false
            setOnClickListener {
                val intent = Intent(this@MyReportsActivity, ItemDetailsActivity::class.java)
                intent.putExtra("type", type)
                intent.putExtra("name", name)
                intent.putExtra("location", location)
                intent.putExtra("description", description)
                startActivity(intent)
            }
        }

        detailsButton.gravity = Gravity.CENTER

        card.addView(typeText)
        card.addView(nameText)
        card.addView(locationText)
        card.addView(detailsButton)

        container.addView(card, cardParams)
    }
}