package com.example.plproject

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val name = findViewById<EditText>(R.id.etSignupName)
        val college = findViewById<EditText>(R.id.etSignupCollege)
        val email = findViewById<EditText>(R.id.etSignupEmail)
        val password = findViewById<EditText>(R.id.etSignupPassword)

        val signupButton = findViewById<Button>(R.id.btnSignup)
        val loginButton = findViewById<Button>(R.id.btnOpenLogin)

        val preferences = getSharedPreferences(
            "CampusFindData",
            MODE_PRIVATE
        )

        signupButton.setOnClickListener {
            val nameText = name.text.toString().trim()
            val collegeText = college.text.toString().trim()
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString()

            when {
                nameText.isEmpty() -> {
                    name.error = "Enter your name"
                }

                collegeText.isEmpty() -> {
                    college.error = "Enter your college"
                }

                !Patterns.EMAIL_ADDRESS.matcher(emailText).matches() -> {
                    email.error = "Enter a valid email"
                }

                passwordText.length < 6 -> {
                    password.error = "Password must be at least 6 characters"
                }

                else -> {
                    preferences.edit()
                        .putString("studentName", nameText)
                        .putString("studentCollege", collegeText)
                        .putString("studentEmail", emailText)
                        .putString("studentPassword", passwordText)
                        .putBoolean("isLoggedIn", true)
                        .apply()

                    Toast.makeText(
                        this,
                        "Account created successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    finish()
                }
            }
        }

        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}