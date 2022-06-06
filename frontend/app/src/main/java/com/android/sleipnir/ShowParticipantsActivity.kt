package com.android.sleipnir

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import com.android.sleipnir.ui.util.HorseItemAdapter
import com.android.sleipnir.ui.util.ParticipantItemAdapter
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ShowParticipantsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_participants)


        val queue = Volley.newRequestQueue(this)
        val sharedPref : SharedPreferences = getSharedPreferences("userPreference",
            AppCompatActivity.MODE_PRIVATE
        )
        val routeId = intent.getIntExtra("routeId", -1)
        val userId = sharedPref.getInt("userId", -1)
        val token = sharedPref.getString("token", "")
        var sureToken: String = ""

        if (token != null)
            sureToken = token


        val genders = listOf(getString(R.string.stallion), getString(R.string.gelding), getString(R.string.mare))

        val horses: ArrayList<JSONObject> = ArrayList()
        val adapter = ParticipantItemAdapter(this, userId, sureToken, genders, horses)
        val list: ListView = findViewById(R.id.participant_container)
        list.setOnItemClickListener { _, _, position, _ ->
            val intnt = Intent(this, ShowDetailedHorse::class.java)
            intnt.putExtra("name", horses[position].getString("name"))
            intnt.putExtra("height", horses[position].getDouble("height"))
            intnt.putExtra("weight", horses[position].getDouble("weight"))
            intnt.putExtra("age", horses[position].getInt("age"))
            intnt.putExtra("coat", horses[position].getInt("coat"))
            intnt.putExtra("gender", horses[position].getInt("gender"))
            intnt.putExtra("breed", horses[position].getString("breed"))
            intnt.putExtra("image", horses[position].getString("image"))
            startActivity(intnt)
        }


        list.adapter = adapter


        val url = "http://10.0.2.2:8000/route/".plus(routeId)
            .plus("/participants")
        val jsonObjectRequest = object: JsonArrayRequest(
            Method.GET, url, null,
            { response ->

                for (i in 0 until response.length()) {
                    val horse = response.getJSONObject(i)

                    horses.add(horse)
                }

                adapter.notifyDataSetChanged()
            },
            { error ->
                Log.d("error", error.toString())
                Toast.makeText(this, getString(R.string.error_get_participants), Toast.LENGTH_SHORT).show()
            }
        )
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                if (token != null)
                    headers["Authorization"] = "Token $token"
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }
}