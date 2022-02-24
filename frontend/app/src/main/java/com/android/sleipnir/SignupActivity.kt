package com.android.sleipnir

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class SignupActivity : AppCompatActivity() {

    private lateinit var submitBtn: Button
    private lateinit var userNameInput: EditText
    private lateinit var firstNameInput: EditText
    private lateinit var lastNameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var repeatedPasswordInput: EditText
    private lateinit var telegramUserInput: EditText
    private lateinit var emailInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val queue = Volley.newRequestQueue(this)

        submitBtn = findViewById(R.id.submitButton)
        submitBtn.setOnClickListener {
            userNameInput = findViewById(R.id.userName)
            firstNameInput = findViewById(R.id.firstName)
            lastNameInput = findViewById(R.id.lastName)
            passwordInput = findViewById(R.id.password)
            repeatedPasswordInput = findViewById(R.id.repeatedPassword)
            telegramUserInput = findViewById(R.id.telegramUser)
            emailInput = findViewById(R.id.email)
            var validated = 1

            if (passwordInput.text.toString() != repeatedPasswordInput.text.toString())
                validated = -1

            if (!telegramUserInput.text.startsWith('@'))
                validated = -2

            if (validated == 1) {
                val url = "http://10.0.2.2:8000/user/signup"
                val user = JSONObject()
                user.put("username", userNameInput.text.toString())
                user.put("first_name", firstNameInput.text.toString())
                user.put("last_name", lastNameInput.text.toString())
                user.put("password", passwordInput.text.toString())
                user.put("email", emailInput.text.toString())

                val json = JSONObject()
                json.put("user", user)
                json.put("telegram_user", telegramUserInput.text.toString())

                val jsonObjectRequest = JsonObjectRequest(
                    Request.Method.POST, url, json,
                    { response ->
                        val intent = Intent(this, SignupActivity::class.java)
                        intent.putExtra("token", response.getString("token"))
                        val user = response.getJSONObject("user")
                        intent.putExtra("userId", user.getInt("id"))
                        startActivity(intent)
                    },
                    { error ->
                        Log.d("error", error.toString())
                    }
                )


                queue.add(jsonObjectRequest)

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                if (validated == -1)
                    Toast.makeText(this, R.string.password_error,Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this, R.string.telegramUser_error,Toast.LENGTH_SHORT).show()
            }
        }
    }
}