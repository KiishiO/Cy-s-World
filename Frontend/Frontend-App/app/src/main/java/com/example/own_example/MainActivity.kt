package com.example.own_example

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var passwordStrengthBar: ProgressBar
    private lateinit var loadingProgress: CircularProgressIndicator
    private lateinit var rememberMeCheckbox: MaterialCheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        val loginCard = findViewById<MaterialCardView>(R.id.loginCard)
        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnSignIn = findViewById<MaterialButton>(R.id.btnSignIn)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        val logo = findViewById<ImageView>(R.id.ivLogo)

        passwordStrengthBar = findViewById(R.id.passwordStrengthBar)
        loadingProgress = findViewById(R.id.loadingProgress)
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox)

        // Set initial states
        loginCard.translationY = 1000f
        logo.scaleX = 0f
        logo.scaleY = 0f

        // Animate logo
        logo.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1000)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                // Animate login card after logo animation
                loginCard.animate()
                    .translationY(0f)
                    .setDuration(1000)
                    .setInterpolator(AccelerateDecelerateInterpolator())
            }

        // Password strength monitoring
        etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.isNotEmpty()) {
                    passwordStrengthBar.visibility = View.VISIBLE
                    updatePasswordStrength(password)
                } else {
                    passwordStrengthBar.visibility = View.GONE
                }
            }
        })

        // Add click animation to sign in button
        btnSignIn.setOnClickListener {
            btnSignIn.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    btnSignIn.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    val username = etUsername.text.toString()
                    val password = etPassword.text.toString()

                    if (username.isEmpty() || password.isEmpty()) {
                        showError("Please fill in all fields")
                        shakeView(if (username.isEmpty()) etUsername else etPassword)
                    } else {
                        showLoginAnimation(btnSignIn)
                    }
                }
                .start()
        }

        tvForgotPassword.setOnClickListener {
            showInfo("Reset password link sent")
        }
    }

    private fun updatePasswordStrength(password: String) {
        val strength = calculatePasswordStrength(password)
        passwordStrengthBar.progress = strength
        when (strength) {
            in 0..33 -> passwordStrengthBar.progressTintList = ColorStateList.valueOf(Color.RED)
            in 34..66 -> passwordStrengthBar.progressTintList = ColorStateList.valueOf(Color.YELLOW)
            else -> passwordStrengthBar.progressTintList = ColorStateList.valueOf(Color.GREEN)
        }
    }

    private fun calculatePasswordStrength(password: String): Int {
        var score = 0
        if (password.length >= 8) score += 20
        if (password.any { it.isUpperCase() }) score += 20
        if (password.any { it.isLowerCase() }) score += 20
        if (password.any { it.isDigit() }) score += 20
        if (password.any { !it.isLetterOrDigit() }) score += 20
        return score
    }

    private fun shakeView(view: View) {
        view.animate()
            .translationX(20f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .translationX(-20f)
                    .setDuration(100)
                    .withEndAction {
                        view.animate()
                            .translationX(0f)
                            .setDuration(100)
                    }
            }
    }

    private fun showLoginAnimation(button: MaterialButton) {
        button.isEnabled = false
        button.text = ""
        loadingProgress.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            loadingProgress.visibility = View.GONE
            button.text = "Sign In"

            if (rememberMeCheckbox.isChecked) {
                showSuccess("Login saved!")
            } else {
                showSuccess("Successfully logged in!")
            }

            Handler(Looper.getMainLooper()).postDelayed({
                button.isEnabled = true
            }, 1000)
        }, 2000)
    }

    private fun showError(message: String) {
        val snackbar = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        )
        snackbar.setBackgroundTint(getColor(R.color.cardinal_red))
        snackbar.show()
    }

    private fun showSuccess(message: String) {
        val snackbar = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        )
        snackbar.setBackgroundTint(Color.parseColor("#4CAF50"))
        snackbar.show()
    }

    private fun showInfo(message: String) {
        val snackbar = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        )
        snackbar.setBackgroundTint(Color.parseColor("#2196F3"))
        snackbar.show()
    }
}