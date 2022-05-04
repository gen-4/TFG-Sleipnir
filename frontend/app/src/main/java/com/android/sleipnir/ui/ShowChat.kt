package com.android.sleipnir.ui

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.TextView
import com.android.sleipnir.ui.util.ChatItemAdapter
import com.android.sleipnir.R
import com.android.volley.toolbox.JsonArrayRequest
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

        val url = "http://10.0.2.2:8000/route/".plus(intent.getIntExtra("routeId", -1))
            .plus("/messages")
        val jsonObjectRequest = object: JsonArrayRequest(
            Method.GET, url, null,
            { response ->

                val messageList: ArrayList<JSONObject> = ArrayList()
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
                list.adapter = ChatItemAdapter(this, messageList)
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