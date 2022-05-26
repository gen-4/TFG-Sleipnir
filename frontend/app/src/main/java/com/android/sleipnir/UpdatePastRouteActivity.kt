package com.android.sleipnir

import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.android.sleipnir.databinding.ActivityUpdatePastRouteBinding
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import org.json.JSONArray
import org.json.JSONObject

class UpdatePastRouteActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityUpdatePastRouteBinding

    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    private lateinit var horseJson: JSONArray
    private var horseId = -1



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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUpdatePastRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val updateBtn: Button = findViewById(R.id.update_button)

        val queue = Volley.newRequestQueue(this)


        val strRoute = intent.getStringExtra("route")
        var jsonRoute = JSONObject()

        if (strRoute != null) {
            jsonRoute = JSONObject(strRoute)
        }



        val routeNameText: TextView = findViewById(R.id.route_name)
        val maxParticipantsInput: EditText = findViewById(R.id.max_participants)
        val dateInput: EditText = findViewById(R.id.date_picker)
        val durationText: TextView = findViewById(R.id.duration_text)

        val intDuration = jsonRoute.getInt("duration")
        val duration = (intDuration / 60).toString().plus(".").plus((intDuration % 60).toString())

        routeNameText.text = jsonRoute.getString("route_name")
        durationText.text = duration.plus(" h")


        val sharedPref : SharedPreferences = applicationContext.getSharedPreferences("userPreference", MODE_PRIVATE)



        val userId = sharedPref.getInt("userId", -1)
        val token = sharedPref.getString("token", "")
        var sureToken = ""
        if (token != null)
            sureToken = token


        getHorses(queue, userId, sureToken)

        updateBtn.setOnClickListener {
            val strRoute = intent.getStringExtra("route")
            var jsonRoute = JSONObject()
            if (strRoute != null) {
                jsonRoute = JSONObject(strRoute)
            }

            val jsonParam = JSONObject()
            jsonParam.put("celebration_date", dateInput.text.toString()
                .replace(" ", "T"))
            jsonParam.put("max_participants", maxParticipantsInput.text.toString().toInt())
            jsonParam.put("horse", horseId)

            val url = "http://10.0.2.2:8000/route/".plus(jsonRoute.getInt("id"))
                .plus("/update")
            val jsonObjectRequest = object: JsonObjectRequest(
                Method.POST, url, jsonParam,
                { response ->
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
                    headers["Authorization"] = "Token $token"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)
        }


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)

        setUpMap()
    }

    private fun setUpMap() {
        val strRoute = intent.getStringExtra("route")
        var jsonRoute = JSONObject()

        if (strRoute != null) {
            jsonRoute = JSONObject(strRoute)
        }

        val points = jsonRoute.getJSONArray("points")
        var point: JSONObject
        for (i in 0 until points.length()) {
            point = points.getJSONObject(i)
            val coord = LatLng(point.getDouble("y_coord"), point.getDouble("x_coord"))
            if (point.getInt("position") == 0)
                placeMarker(coord, true)
            else
                placeMarker(coord, false)
        }
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        return false
    }

    private fun placeMarker(coord: LatLng, isFirst: Boolean) {
        val markerOptions = MarkerOptions().position(coord)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
        if (isFirst)
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        mMap.addMarker(markerOptions)
        if (isFirst)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coord, 12f))
    }
}