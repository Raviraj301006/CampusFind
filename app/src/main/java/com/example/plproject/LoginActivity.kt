package com.example.plproject

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val email = findViewById<EditText>(R.id.emailInput)
        val password = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerLink = findViewById<TextView>(R.id.registerLink)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)

        loginButton.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString()

            when {
                emailText.isEmpty() -> email.error = "Enter your email"
                !Patterns.EMAIL_ADDRESS.matcher(emailText).matches() ->
                    email.error = "Enter a valid email"
                passwordText.isEmpty() -> password.error = "Enter your password"

                else -> {
                    val preferences = getSharedPreferences(
                        "CampusFindData",
                        MODE_PRIVATE
                    )

                    val savedEmail = preferences.getString("studentEmail", "")
                    val savedPassword = preferences.getString("studentPassword", "")

                    if (emailText.equals(savedEmail, ignoreCase = true) &&
                        passwordText == savedPassword &&
                        !savedEmail.isNullOrEmpty()
                    ) {
                        preferences.edit()
                            .putBoolean("isLoggedIn", true)
                            .apply()

                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Account not found or password is incorrect.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        forgotPassword.setOnClickListener {
            Toast.makeText(
                this,
                "Password recovery is not available yet.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}