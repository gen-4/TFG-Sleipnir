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
            emailInput = findViewById(R.id.email)

            val url = "http://10.0.2.2:8000/user/signup"
            val user = JSONObject()
            user.put("username", userNameInput.text.toString())
            user.put("first_name", firstNameInput.text.toString())
            user.put("last_name", lastNameInput.text.toString())
            user.put("password", passwordInput.text.toString())
            user.put("email", emailInput.text.toString())

            val json = JSONObject()
            json.put("user", user)

            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST, url, json,
                { response ->
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                },
                { error ->
                    Log.d("error", error.toString())
                    Toast.makeText(this, getString(R.string.error_signup), Toast.LENGTH_SHORT).show()
                }
            )


            queue.add(jsonObjectRequest)


        }
    }
}