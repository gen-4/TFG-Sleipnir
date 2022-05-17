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
import com.android.sleipnir.databinding.ActivityJoinRouteBinding
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import org.json.JSONArray
import org.json.JSONObject

class JoinRouteActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityJoinRouteBinding

    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJoinRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val showChatButton: Button = findViewById(R.id.show_chat_button)
        val showParticipantsBtn: Button = findViewById(R.id.show_participants_button)


        val queue = Volley.newRequestQueue(this)


        val strRoute = intent.getStringExtra("route")
        var jsonRoute = JSONObject()

        if (strRoute != null) {
            jsonRoute = JSONObject(strRoute)
        }

        showChatButton.setOnClickListener {
            val intnt = Intent(this, ShowChat::class.java)
            intnt.putExtra("routeId", jsonRoute.getInt("id"))
            intnt.putExtra("creator", jsonRoute.getInt("creator"))
            startActivity(intnt)
        }

        showParticipantsBtn.setOnClickListener {
            val intnt = Intent(this, ShowParticipantsActivity::class.java)
            intnt.putExtra("routeId", jsonRoute.getInt("id"))
            startActivity(intnt)
        }


        val routeNameText: TextView = findViewById(R.id.route_name)
        val maxParticipantsText: TextView = findViewById(R.id.max_participants_text)
        val currParticipantsText: TextView = findViewById(R.id.curr_participants)
        val dateText: TextView = findViewById(R.id.date_text)
        val durationText: TextView = findViewById(R.id.duration_text)

        val intDuration = jsonRoute.getInt("duration")
        val duration = (intDuration / 60).toString().plus(".").plus((intDuration % 60).toString())

        routeNameText.text = jsonRoute.getString("route_name")
        maxParticipantsText.text = jsonRoute.getInt("max_participants").toString()
        currParticipantsText.text = jsonRoute.getInt("current_participants").toString()
        dateText.text = jsonRoute.getString("celebration_date").replace('T', ' ')
        durationText.text = duration.plus(" h")


        val joinButton: Button = findViewById(R.id.join_button)
        val leaveButton: Button = findViewById(R.id.leave_button)

        val sharedPref : SharedPreferences = applicationContext.getSharedPreferences("userPreference", MODE_PRIVATE)




        val horses = ArrayList<String>()
        var horseJson = JSONArray()
        val userId = sharedPref.getInt("userId", -1)
        val token = sharedPref.getString("token", "")
        var sureToken: String = ""
        if (token != null)
            sureToken = token

        val urlGetHorses = "http://10.0.2.2:8000/user/".plus(userId)
            .plus("/horses")
        val jsonRequest = object: JsonArrayRequest(
            Method.GET, urlGetHorses, null,
            { response ->
                horseJson = response
                for (i in 0 until response.length()) {
                    horses.add(response.getJSONObject(i).getString("name"))
                }
            },
            { error ->
                Log.d("error", error.toString())
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

        queue.add(jsonRequest)


        val horseSpinner: Spinner = findViewById(R.id.horse_picker)
        val horseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, horses)
        horseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        horseSpinner.adapter = horseAdapter


        var horseId = -1

        horseSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                horseId = horseJson.getJSONObject(position).getInt("id")
            }
        }








        val url = "http://10.0.2.2:8000/route/".plus(jsonRoute.getInt("id").toString())
            .plus("/has_joined/").plus(sharedPref.getInt("userId", -1).toString())
        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET, url, null,
            { response ->
                val result = response.getInt("joined")
                if (result == 1) {
                    leaveButton.visibility = View.VISIBLE
                } else if (result == -1) {
                    joinButton.visibility = View.VISIBLE
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





        leaveButton.setOnClickListener {

            val url = "http://10.0.2.2:8000/route/".plus(jsonRoute.getInt("id").toString()).plus("/leave_route")
            val json = JSONObject()
            json.put("user", intent.getIntExtra("userId", -1))

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


        joinButton.setOnClickListener {

            val url = "http://10.0.2.2:8000/route/".plus(jsonRoute.getInt("id").toString()).plus("/join_route")
            val json = JSONObject()
            json.put("user", userId)
            json.put("horse", horseId)

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