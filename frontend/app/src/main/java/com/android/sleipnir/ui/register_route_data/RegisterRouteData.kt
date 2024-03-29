package com.android.sleipnir.ui.register_route_data

import android.Manifest
import android.app.Activity
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.sleipnir.R
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONArray
import org.json.JSONObject

class RegisterRouteData : Fragment() {

    private lateinit var mMap: GoogleMap

    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var isRecording = false
    private var isFirst = true

    private var distance = Float.fromBits(0)
    private var avgSpeed = Float.fromBits(0)

    private lateinit var startBtn: Button
    private lateinit var chrono: Chronometer
    private lateinit var recordName: EditText
    private lateinit var distanceText: TextView
    private lateinit var avgSpeedText: TextView

    private var pointList = ArrayList<LatLng>()
    private var pointAltitudeList = ArrayList<Double>()
    private val lineOptions = PolylineOptions()

    private val observers = ArrayList<String>()

    private lateinit var route: Polyline


    companion object {
        private const val LOCATION_REQUEST_CODE = 1
        private const val DELAY_MINUTES = 2
        private const val POINTS_TRESHOLD = 2
    }

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        lineOptions.color(Color.RED)
        lineOptions.geodesic(true)
        lineOptions.width(20f)
        route = mMap.addPolyline(lineOptions)

        setUpMap()
    }

    private fun setUpMap() {

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                requireContext() as Activity, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return
        }

        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(requireContext() as Activity) { location ->

            if (location != null) {
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 12f))
            }

        }// Wrong format, no value for you.
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_route_data, container, false)
    }



    private fun sendNotifications() {

        val queue = Volley.newRequestQueue(requireContext())
        val sharedPref : SharedPreferences = requireContext().getSharedPreferences("userPreference",
            AppCompatActivity.MODE_PRIVATE
        )
        val userId = sharedPref.getInt("userId", -1)
        val token = sharedPref.getString("token", "")
        var sureToken: String = ""

        if (token != null)
            sureToken = token

        val url = "http://10.0.2.2:8000/user/".plus(userId)
            .plus("/modify_last_location")

        while (true) {
            Thread.sleep((DELAY_MINUTES * 60000).toLong())

            val currentLocation = fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity() as Activity) { location ->

                if (location != null) {
                    lastLocation = location
                    val jsonObj = JSONObject()
                    jsonObj.put("last_x_coord", location.longitude)
                    jsonObj.put("last_y_coord", location.latitude)

                    val jsonObjectRequest = object: JsonObjectRequest(
                        Method.POST, url, jsonObj,
                        { response ->

                        },
                        { error ->
                            Log.d("error", error.toString())
                            Toast.makeText(requireActivity(), requireActivity().getString(R.string.error_modify_location), Toast.LENGTH_SHORT).show()
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
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        recordName = requireActivity().findViewById(R.id.record_name)
        distanceText = requireActivity().findViewById(R.id.km_ridden)
        avgSpeedText = requireActivity().findViewById(R.id.average_speed)
        updateData()

        chrono = requireActivity().findViewById(R.id.total_duration)
        startBtn = requireActivity().findViewById(R.id.record_button)
        startBtn.setOnClickListener {

            val locRequest = LocationRequest()
            locRequest.interval = 180000
            locRequest.fastestInterval = 60000
            locRequest.smallestDisplacement = 90f
            locRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    for (location in locationResult.locations) {

                        val point = LatLng(location.latitude, location.longitude)
                        pointAltitudeList.add(location.altitude)
                        if (pointList.isNotEmpty()) {
                            val startLocation = Location("")
                            startLocation.latitude = pointList.last().latitude
                            startLocation.longitude = pointList.last().longitude

                            val segmentDistance = startLocation.distanceTo(location)
                            distance += segmentDistance

                            val time: String = chrono.text.toString()
                            avgSpeed = (distance / 1000) / (getSecondsFromDurationString(time) / 3600)

                        }

                        pointList.add(point)
                        route.points = pointList
                    }
                }
            }

            val thrd = Thread {sendNotifications()}

            if (!isRecording) {
                isRecording = true
                isFirst = false
                chrono.base = SystemClock.elapsedRealtime()
                chrono.start()
                startBtn.text = getString(R.string.stop_record)

                //Start thread
                thrd.start()

                fusedLocationClient.requestLocationUpdates(locRequest,
                    locationCallback,
                    Looper.getMainLooper())

            } else {
                isRecording = false

                //Kill thread
                thrd.interrupt()

                chrono.stop()
                startBtn.text = getString(R.string.start_record)
                fusedLocationClient.removeLocationUpdates(locationCallback)
                if (!isFirst) {
                    startBtn.visibility = View.GONE
                    if (pointList.size >= POINTS_TRESHOLD)
                        sendData()
                }
            }

        }
    }

    fun getSecondsFromDurationString(value: String): Int {
        val parts = value.split(":").toTypedArray()

        if (parts.size < 2 || parts.size > 3) return 0
        var seconds = 0
        var minutes = 0
        var hours = 0
        if (parts.size == 2) {
            seconds = parts[1].toInt()
            minutes = parts[0].toInt()
        } else if (parts.size == 3) {
            seconds = parts[2].toInt()
            minutes = parts[1].toInt()
            hours = parts[0].toInt()
        }
        return seconds + minutes * 60 + hours * 3600
    }

    private fun sendData() {
        val jsonArray = JSONArray()
        for ((i, point) in pointList.withIndex()) {
            val jsonObj = JSONObject()
            jsonObj.put("x_coord", point.longitude)
            jsonObj.put("y_coord", point.latitude)
            jsonObj.put("position", i)
            jsonObj.put("altitude", pointAltitudeList[i])
            jsonArray.put(jsonObj)
        }


        val sharedPref : SharedPreferences = requireActivity().getSharedPreferences("userPreference",
            AppCompatActivity.MODE_PRIVATE
        )
        val queue = Volley.newRequestQueue(requireContext())
        val url = "http://10.0.2.2:8000/route/register_route_data"

        val json = JSONObject()
        json.put("rider", sharedPref.getInt("userId", -1))
        json.put("record_name", recordName.text)
        json.put("distance", distance)
        json.put("duration", getSecondsFromDurationString(chrono.text.toString()))
        json.put("avg_speed", avgSpeed)
        json.put("points", jsonArray)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST, url, json,
            { response ->

            },
            { error ->
                Log.d("error", error.toString())
                Toast.makeText(requireActivity(), requireActivity().getString(R.string.error_register_data), Toast.LENGTH_SHORT).show()
            }
        )
        {
            override fun getHeaders(): MutableMap<String, String> {
                val token = sharedPref.getString("token", "")
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Token $token"
                return headers
            }
        }

        queue.add(jsonObjectRequest)
    }

    private fun updateData() {
        distanceText.text = distance.toString().plus(" m")
        avgSpeedText.text = avgSpeed.toString().plus(" km/h")
    }
}