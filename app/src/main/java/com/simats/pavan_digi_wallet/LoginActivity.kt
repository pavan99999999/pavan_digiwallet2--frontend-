package com.simats.pavan_digi_wallet

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_email)
        val etPassword = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_password)

        findViewById<Button>(R.id.btn_login).setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Direct Login with Backend
            val loginRequest = LoginRequest(email, password)
            RetrofitClient.apiService.login(loginRequest).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        val authResponse = response.body()
                        if (authResponse?.accessToken != null) {
                            val sharedPref = getSharedPreferences("digi_wallet", android.content.Context.MODE_PRIVATE)
                            sharedPref.edit().apply {
                                putInt("user_id", authResponse.userId ?: 1)
                                putString("user_name", authResponse.name)
                                putString("access_token", authResponse.accessToken)
                                apply()
                            }
                            Toast.makeText(this@LoginActivity, "Welcome ${authResponse.name}!", Toast.LENGTH_SHORT).show()
                            goToHome()
                        } else {
                            Toast.makeText(this@LoginActivity, authResponse?.error ?: "Invalid Credentials", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Login Failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
        
        findViewById<TextView>(R.id.btn_signup_link).setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }


    private fun goToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(AppCompatActivity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
