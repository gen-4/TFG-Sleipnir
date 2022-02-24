package com.android.sleipnir

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var signupBtn: Button
    private lateinit var submitBtn: Button
    private lateinit var userNameInput: EditText
    private lateinit var passwordInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val queue = Volley.newRequestQueue(this)

        signupBtn = findViewById(R.id.SignupButton)
        signupBtn.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        submitBtn = findViewById(R.id.submitButton)
        submitBtn.setOnClickListener {
            userNameInput = findViewById(R.id.userName)
            passwordInput = findViewById(R.id.password)

            val url = "http://10.0.2.2:8000/user/login"
            val json = JSONObject()
            json.put("username", userNameInput.text.toString())
            json.put("password", passwordInput.text.toString())

            val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, json,
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
        }
    }
}