
package com.example.plproject

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

class MainActivity : AppCompatActivity() {

    private var selectedFilter = "All"
    private var searchText = ""

    private lateinit var allItemsButton: Button
    private lateinit var lostItemsButton: Button
    private lateinit var foundItemsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        allItemsButton = findViewById(R.id.allItemsButton)
        lostItemsButton = findViewById(R.id.lostItemsButton)
        foundItemsButton = findViewById(R.id.foundItemsButton)

        updateWelcomeMessage()

        // Correct system navigation bar spacing
        val mainRoot = findViewById<LinearLayout>(R.id.mainRoot)

        val initialLeft = mainRoot.paddingLeft
        val initialTop = mainRoot.paddingTop
        val initialRight = mainRoot.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(mainRoot) { view, insets ->

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

        ViewCompat.requestApplyInsets(mainRoot)

        // Hide old sample cards
        findViewById<View>(R.id.recentItem1).visibility = View.GONE
        findViewById<View>(R.id.recentItem2).visibility = View.GONE

        // Bottom Navigation
        findViewById<View>(R.id.navHome).setOnClickListener {
            // Already on Home
        }

        findViewById<View>(R.id.navBrowse).setOnClickListener {
            startActivity(Intent(this, BrowseItemsActivity::class.java))
        }

        findViewById<View>(R.id.navReport).setOnClickListener {
            startActivity(Intent(this, ReportItemActivity::class.java))
        }

        findViewById<View>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Report button
        findViewById<Button>(R.id.reportButton).setOnClickListener {
            startActivity(Intent(this, ReportItemActivity::class.java))
        }

        // Filters
        allItemsButton.setOnClickListener {
            selectedFilter = "All"
            displayReports()
        }

        lostItemsButton.setOnClickListener {
            selectedFilter = "Lost"
            displayReports()
        }

        foundItemsButton.setOnClickListener {
            selectedFilter = "Found"
            displayReports()
        }

        // Search
        findViewById<EditText>(R.id.searchBox)
            .addTextChangedListener(object : TextWatcher {

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
                    searchText = s.toString().trim()
                    displayReports()
                }

                override fun afterTextChanged(s: Editable?) {}
            })
    }

    private fun updateWelcomeMessage() {

        val preferences = getSharedPreferences(
            "CampusFindData",
            MODE_PRIVATE
        )

        val studentName = preferences.getString(
            "studentName",
            "Student"
        ) ?: "Student"

        val firstName = studentName.trim()
            .split("\\s+".toRegex())
            .firstOrNull() ?: "Student"

        findViewById<TextView>(R.id.welcomeText).text =
            "Hello, $firstName!"
    }

    override fun onResume() {
        super.onResume()

        if (::allItemsButton.isInitialized) {
            updateWelcomeMessage()
            displayReports()
        }
    }

    private fun displayReports() {

        val activeContainer = findViewById<LinearLayout>(
            R.id.savedReportsContainer
        )

        val resolvedContainer = findViewById<LinearLayout>(
            R.id.resolvedReportsContainer
        )

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

            if (
                selectedFilter != "All" &&
                !type.equals(selectedFilter, ignoreCase = true)
            ) continue

            val searchable = "$name $location $description $type"

            if (
                searchText.isNotEmpty() &&
                !searchable.contains(searchText, ignoreCase = true)
            ) continue

            val isResolved = status.equals("Resolved", true)

            if (isResolved) {
                createReportCard(
                    resolvedContainer,
                    type,
                    name,
                    location,
                    description,
                    true
                )
                resolvedCount++
            } else {
                createReportCard(
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
            addEmptyMessage(
                activeContainer,
                "No active reports found."
            )
        }

        if (resolvedCount == 0) {
            addEmptyMessage(
                resolvedContainer,
                "No resolved reports yet."
            )
        }
    }

    private fun addEmptyMessage(
        container: LinearLayout,
        message: String
    ) {

        val text = TextView(this).apply {
            this.text = message
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.GRAY)
            setPadding(16, 24, 16, 24)
        }

        container.addView(text)
    }

    private fun createReportCard(
        container: LinearLayout,
        type: String,
        name: String,
        location: String,
        description: String,
        isResolved: Boolean
    ) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 14, 16, 14)

            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 16f
                setStroke(1, Color.LTGRAY)
            }
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.bottomMargin = 12

        val statusText = TextView(this).apply {

            text = if (isResolved) {
                "RESOLVED"
            } else {
                type.uppercase()
            }

            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD

            setTextColor(
                when {
                    isResolved -> Color.rgb(4, 120, 87)
                    type.equals("Lost", true) ->
                        Color.rgb(185, 28, 28)
                    else -> Color.rgb(4, 120, 87)
                }
            )
        }

        val nameText = TextView(this).apply {
            text = name
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 8, 0, 6)
        }

        val locationText = TextView(this).apply {
            text = "Location: $location"
            textSize = 13f
        }

        card.addView(statusText)
        card.addView(nameText)
        card.addView(locationText)

        val detailsButton = Button(this).apply {
            text = "View Details"
            isAllCaps = false
        }

        detailsButton.setOnClickListener {
            openDetails(type, name, location, description)
        }

        card.addView(detailsButton)

        card.setOnClickListener {
            openDetails(type, name, location, description)
        }

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
