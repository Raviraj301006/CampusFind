
package com.example.plproject

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONArray

class BrowseItemsActivity : AppCompatActivity() {

    private lateinit var activeContainer: LinearLayout
    private lateinit var resolvedContainer: LinearLayout
    private lateinit var searchBox: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse_items)

        activeContainer = findViewById(R.id.browseReportsContainer)
        resolvedContainer = findViewById(R.id.browseResolvedReportsContainer)
        searchBox = findViewById(R.id.browseSearch)

        // Adjust screen for the phone navigation bar
        val contentRoot = findViewById<View>(android.R.id.content)

        val initialLeft = contentRoot.paddingLeft
        val initialTop = contentRoot.paddingTop
        val initialRight = contentRoot.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(contentRoot) { view, insets ->

            val navigationBarHeight = insets.getInsets(
                WindowInsetsCompat.Type.navigationBars()
            ).bottom

            view.setPadding(
                initialLeft,
                initialTop,
                initialRight,
                navigationBarHeight
            )

            insets
        }

        ViewCompat.requestApplyInsets(contentRoot)

        // Bottom navigation
        findViewById<View>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        findViewById<View>(R.id.navBrowse).setOnClickListener {
            // Already on Browse
        }

        findViewById<View>(R.id.navReport).setOnClickListener {
            startActivity(Intent(this, ReportItemActivity::class.java))
        }

        findViewById<View>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Search
        searchBox.addTextChangedListener(object :
            android.text.TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                displayReports()
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        displayReports()
    }

    override fun onResume() {
        super.onResume()

        if (::activeContainer.isInitialized) {
            displayReports()
        }
    }

    private fun displayReports() {

        activeContainer.removeAllViews()
        resolvedContainer.removeAllViews()

        val preferences = getSharedPreferences(
            "CampusFindData",
            MODE_PRIVATE
        )

        val reports = try {
            JSONArray(
                preferences.getString("reports", "[]") ?: "[]"
            )
        } catch (e: Exception) {
            JSONArray()
        }

        val searchText = searchBox.text.toString().trim()

        var activeCount = 0
        var resolvedCount = 0

        for (i in reports.length() - 1 downTo 0) {

            val report = reports.optJSONObject(i) ?: continue

            val type = report.optString("type", "")
            val name = report.optString("name", "")
            val location = report.optString("location", "")
            val description = report.optString("description", "")
            val status = report.optString("status", "Active")

            if (name.isEmpty()) continue

            val searchable = "$name $location $description $type"

            if (
                searchText.isNotEmpty() &&
                !searchable.contains(searchText, ignoreCase = true)
            ) continue

            val isResolved = status.equals("Resolved", true)

            if (isResolved) {
                addReportCard(
                    resolvedContainer,
                    type,
                    name,
                    location,
                    description,
                    true
                )
                resolvedCount++
            } else {
                addReportCard(
                    activeContainer,
                    type,
                    name,
                    location,
                    description,
                    false
                )
                activeCount++
            }
        }

        if (activeCount == 0) {
            addEmptyMessage(activeContainer, "No active reports found.")
        }

        if (resolvedCount == 0) {
            addEmptyMessage(resolvedContainer, "No resolved reports yet.")
        }
    }

    private fun addEmptyMessage(
        container: LinearLayout,
        message: String
    ) {
        val text = TextView(this).apply {
            this.text = message
            textSize = 14f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(20, 30, 20, 30)
        }

        container.addView(text)
    }

    private fun addReportCard(
        container: LinearLayout,
        type: String,
        name: String,
        location: String,
        description: String,
        isResolved: Boolean
    ) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            setBackgroundColor(Color.WHITE)
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.bottomMargin = 16

        val status = TextView(this).apply {
            text = if (isResolved) "RESOLVED" else type.uppercase()
            textSize = 12f
            setTypeface(null, Typeface.BOLD)

            setTextColor(
                when {
                    isResolved -> Color.rgb(4, 120, 87)
                    type.equals("Lost", true) ->
                        Color.rgb(185, 28, 28)
                    else -> Color.rgb(4, 120, 87)
                }
            )
        }

        val itemName = TextView(this).apply {
            text = name
            textSize = 18f
            setTextColor(Color.rgb(23, 37, 84))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 10, 0, 0)
        }

        val itemLocation = TextView(this).apply {
            text = "Location: $location"
            textSize = 14f
            setTextColor(Color.rgb(100, 116, 139))
            setPadding(0, 8, 0, 0)
        }

        val itemDescription = TextView(this).apply {
            text = description
            textSize = 14f
            setPadding(0, 8, 0, 0)
        }

        card.addView(status)
        card.addView(itemName)
        card.addView(itemLocation)
        card.addView(itemDescription)

        val detailsButton = Button(this).apply {
            text = "View Details"
            isAllCaps = false
        }

        detailsButton.setOnClickListener {
            openDetails(type, name, location, description)
        }

        card.addView(detailsButton)

        container.addView(card, params)
    }

    private fun openDetails(
        type: String,
        name: String,
        location: String,
        description: String
    ) {

        val intent = Intent(
            this,
            ItemDetailsActivity::class.java
        )

        intent.putExtra("type", type)
        intent.putExtra("name", name)
        intent.putExtra("location", location)
        intent.putExtra("description", description)

        startActivity(intent)
    }
}
