package com.android.sleipnir

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.android.sleipnir.databinding.ActivityDetailedRegisterBinding
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.*
import org.json.JSONArray
import org.json.JSONObject

class DetailedRegisterActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityDetailedRegisterBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailedRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        val sharedPref : SharedPreferences = applicationContext.getSharedPreferences("userPreference", MODE_PRIVATE)
        val queue = Volley.newRequestQueue(this)

        val url = "http://10.0.2.2:8000/route/detailed_record/"
            .plus(intent.getIntExtra("recordId", -1).toString())

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET, url, null,
            { response ->

                val recordName: TextView = findViewById(R.id.record_name)
                val avgSpeedText: TextView = findViewById(R.id.average_speed)
                val durationText: TextView = findViewById(R.id.record_duration)
                val dateText: TextView = findViewById(R.id.record_date)
                val distanceText: TextView = findViewById(R.id.km_ridden)

                recordName.text = response.getString("record_name")
                avgSpeedText.text = response.getDouble("avg_speed").toString().plus(" km/h")
                val duration = response.getInt("duration")
                val tmp = duration / 60
                val seconds = duration % 60
                val hours = tmp / 60
                val minutes = tmp % 60
                durationText.text = hours.toString().plus(":").plus(minutes.toString()).plus(":").plus(seconds.toString())
                dateText.text = response.getString("date")
                distanceText.text = response.getInt("distance").toString().plus(" m")

                setUpMap(response.getJSONArray("points"))

                val altitudeBtn: Button = findViewById(R.id.access_altitude_btn)
                altitudeBtn.setOnClickListener {
                    val intnt = Intent(this, AltitudeChartActivity::class.java)
                    intnt.putExtra("points", response.getJSONArray("points").toString())
                    startActivity(intnt)
                }
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

    private fun setUpMap(points: JSONArray) {

        val pointList = ArrayList<LatLng>()

        for (i in 0 until points.length()) {
            val point = points.getJSONObject(i)
            pointList.add(LatLng(point.getDouble("y_coord"), point.getDouble("x_coord")))
            if (point.getInt("position") == 0)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pointList.first(), 12f))

        }

        val lineOptions = PolylineOptions()
        lineOptions.color(Color.RED)
        lineOptions.geodesic(true)
        lineOptions.width(20f)
        lineOptions.addAll(pointList)
        mMap.addPolyline(lineOptions)
    }

}