package com.android.sleipnir.ui

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import com.android.sleipnir.ui.util.ChatItemAdapter
import com.android.sleipnir.R
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class ShowChat : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_chat)


        val queue = Volley.newRequestQueue(this)
        val sharedPref : SharedPreferences = applicationContext.getSharedPreferences("userPreference", MODE_PRIVATE)
        val userName = sharedPref.getString("userName", "")

        val messageList: ArrayList<JSONObject> = ArrayList()
        val adapter = ChatItemAdapter(this, messageList)


        val sendButton: Button = findViewById(R.id.message_send_btn)
        sendButton.setOnClickListener {
            val msgInput: EditText = findViewById(R.id.message_input)

            val url = "http://10.0.2.2:8000/route/".plus(intent.getIntExtra("routeId", -1))
                .plus("/post_message")

            val jsonBody = JSONObject()
            jsonBody.put("writer", sharedPref.getInt("userId", -1))
            jsonBody.put("message", msgInput.text)

            val jsonObjectRequest = object: JsonObjectRequest(
                Method.POST, url, jsonBody,
                { response ->

                    val userId = sharedPref.getInt("userId", -1)
                    val writer = response.getJSONObject("writer")
                    val writerId = writer.getInt("id")
                    val creatorId = intent.getIntExtra("creator", -1)

                    if (writerId == userId) {
                        if (creatorId == userId)
                            response.put("type", 0)

                        else
                            response.put("type", 1)

                    } else {
                        if (creatorId == writerId)
                            response.put("type", 2)

                        else
                            response.put("type", 3)

                    }

                    messageList.add(response)
                    adapter.notifyDataSetChanged()

                },
                { error ->
                    Log.d("error", error.toString())
                }
            )
            {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    val token = sharedPref.getString("token", "")
                    if (token != null)
                        headers["Authorization"] = "Token $token"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)

            sendButton.text = ""
        }


        val url = "http://10.0.2.2:8000/route/".plus(intent.getIntExtra("routeId", -1))
            .plus("/messages")
        val jsonObjectRequest = object: JsonArrayRequest(
            Method.GET, url, null,
            { response ->

                val userId = sharedPref.getInt("userId", -1)

                for (i in 0 until response.length()) {
                    val message = response.getJSONObject(i)
                    val writer = message.getJSONObject("writer")
                    val writerId = writer.getInt("id")
                    val creatorId = intent.getIntExtra("creator", -1)

                    if (writerId == userId) {
                        if (creatorId == userId)
                            message.put("type", 0)

                        else
                            message.put("type", 1)

                    } else {
                        if (creatorId == writerId)
                            message.put("type", 2)

                        else
                            message.put("type", 3)

                    }

                    messageList.add(message)
                }

                val list: ListView = findViewById(R.id.chat_container)
                list.adapter = adapter
            },
            { error ->
                Log.d("error", error.toString())
            }
        )
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                val token = sharedPref.getString("token", "")
                if (token != null)
                    headers["Authorization"] = "Token $token"
                return headers
            }
        }

        queue.add(jsonObjectRequest)
    }


}