package com.android.sleipnir

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FillRouteInfoActivity : AppCompatActivity() {

    private lateinit var x_coord: TextView
    private lateinit var y_coord: TextView

    private lateinit var durationPicker: EditText
    private lateinit var submitBtn: Button

    private lateinit var routeName: EditText
    private lateinit var maxParcipants: EditText
    private lateinit var date: EditText

    private lateinit var horseJson: JSONArray
    var horseId = -1


    private fun onResponse(horses: List<String>) {
        val horseSpinner: Spinner = findViewById(R.id.horse_picker)
        val horseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, horses)
        horseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        horseSpinner.adapter = horseAdapter



        horseSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                horseId = horseJson.getJSONObject(position).getInt("id")
            }
        }
    }

    private fun getHorses(queue: RequestQueue, userId: Int, token: String) {
        val horses = ArrayList<String>()

        val url = "http://10.0.2.2:8000/user/".plus(userId)
            .plus("/horses")
        val jsonObjectRequest = object: JsonArrayRequest(
            Method.GET, url, null,
            { response ->
                horseJson = response
                for (i in 0 until response.length()) {
                    horses.add(response.getJSONObject(i).getString("name"))
                }
                onResponse(horses.toList())
            },
            { error ->
                Log.d("error", error.toString())
            }
        )
        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Token $token"
                return headers
            }
        }

        queue.add(jsonObjectRequest)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_route_info)

        val pointList = intent.getParcelableArrayListExtra<LatLng>("points")
        x_coord = findViewById(R.id.x_cord)
        y_coord = findViewById(R.id.y_cord)
        x_coord.text = pointList?.get(0)?.longitude.toString()
        y_coord.text = pointList?.get(0)?.latitude.toString()

        durationPicker = findViewById(R.id.duration)

        val sharedPref : SharedPreferences = applicationContext.getSharedPreferences("userPreference", MODE_PRIVATE)

        val queue = Volley.newRequestQueue(this)



        val userId = sharedPref.getInt("userId", -1)
        val token = sharedPref.getString("token", "")
        var sureToken = ""

        if (token != null)
            sureToken = token

        getHorses(queue, userId, sureToken)



        submitBtn = findViewById(R.id.submit_route_btn)
        submitBtn.setOnClickListener {


            val duration = durationPicker.text

            routeName = findViewById(R.id.route_name)
            maxParcipants = findViewById(R.id.max_participants)
            date = findViewById(R.id.date_picker)

            val celebrationDate = date.text.toString()

            val routeNameText = routeName.text
            val maxParticipantsNumber = maxParcipants.text

            val jsonList = ArrayList<JSONObject>()
            if (pointList != null) {
                for ((i, point) in pointList.withIndex()) {
                    val pointJson = JSONObject()
                    pointJson.put("x_coord", point.longitude)
                    pointJson.put("y_coord", point.latitude)
                    pointJson.put("position", i)
                    jsonList.add(pointJson)
                }
            }

            val url = "http://10.0.2.2:8000/route/create_route"
            val json = JSONObject()
            json.put("creator", sharedPref.getInt("userId", -1))
            json.put("route_name", routeNameText.toString())
            json.put("max_participants", maxParticipantsNumber)
            json.put("duration", duration)
            json.put("celebration_date", celebrationDate.replace(" ", "T")
                .replace("/", "-"))
            json.put("horse", horseId)
            json.put("points", jsonList)

            val jsonObjectRequest = object: JsonObjectRequest(
                Method.POST, url, json,
                { reponse ->
                    val intnt = Intent(this, DrawerActivity::class.java)
                    startActivity(intnt)
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


}