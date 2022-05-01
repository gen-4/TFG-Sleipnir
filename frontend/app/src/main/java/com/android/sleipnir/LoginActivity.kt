package com.android.sleipnir

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
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

                    val sharedPref : SharedPreferences = applicationContext.getSharedPreferences("userPreference", MODE_PRIVATE)
                    val user = response.getJSONObject("user")
                    with (sharedPref.edit()) {
                        putString("token", response.getString("token"))
                        putInt("userId", user.getInt("id"))
                        putString("userName", userNameInput.text.toString())
                        commit()
                    }

                    val intent = Intent(this, DrawerActivity::class.java)
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