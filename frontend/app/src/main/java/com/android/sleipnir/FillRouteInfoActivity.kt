package com.android.sleipnir

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FillRouteInfoActivity : AppCompatActivity() {

    private lateinit var x_coord: TextView
    private lateinit var y_coord: TextView

    private lateinit var durationPicker: TimePicker
    private lateinit var submitBtn: Button

    private lateinit var routeName: EditText
    private lateinit var maxParcipants: NumberPicker
    private lateinit var date: DatePicker
    private lateinit var time: TimePicker


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
        durationPicker.setIs24HourView(true)

        val queue = Volley.newRequestQueue(this)

        submitBtn = findViewById(R.id.submit_route_btn)
        submitBtn.setOnClickListener {


            val durationHours = durationPicker.hour
            val durationMinutes = durationPicker.minute

            routeName = findViewById(R.id.route_name)
            maxParcipants = findViewById(R.id.max_participants)
            date = findViewById(R.id.date_picker)
            time = findViewById(R.id.time_picker)

            val day = date.dayOfMonth
            val month = date.month
            val year = date.year
            val timeHour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                time.hour
            else
                0
            val timeMinute = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                time.minute
            else
                0

            val celebrationDate = LocalDateTime.of(year, month, day, timeHour, timeMinute)

            val duration = durationHours*60+durationMinutes
            val routeNameText = routeName.text
            val maxParticipantsNumber = maxParcipants.value

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
            json.put("creator", intent.getIntExtra("userId", -1))
            json.put("route_name", routeNameText.toString())
            json.put("max_participants", maxParticipantsNumber)
            json.put("duration", duration)
            json.put("celebration_date", celebrationDate)
            json.put("points", jsonList)

            val jsonObjectRequest = object: JsonObjectRequest(
                Request.Method.POST, url, json,
                { reponse ->
                    val intnt = Intent(this, DrawerActivity::class.java)
                    intnt.putExtra("userId", intent.getIntExtra("userId", -1))
                    intnt.putExtra("userName", intent.getStringExtra("userName"))
                    intnt.putExtra("token", intent.getStringExtra("token"))
                    startActivity(intnt)
                },
                { error ->
                    Log.d("error", error.toString())
                }
            )
            {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    val token = intent.getStringExtra("token")
                    if (token != null)
                        headers["Authorization"] = "Token $token"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)

        }
    }
}