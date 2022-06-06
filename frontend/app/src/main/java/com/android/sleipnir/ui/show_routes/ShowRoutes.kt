package com.android.sleipnir.ui.show_routes

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.sleipnir.JoinRouteActivity
import com.android.sleipnir.R
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

class ShowRoutes : Fragment(), GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap

    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var markerList = ArrayList<Marker>()
    private lateinit var routeList: JSONArray


    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)

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

        }

        val searchRouteBtn: Button = requireActivity().findViewById(R.id.code_btn)
        searchRouteBtn.setOnClickListener {
            val codeInput: EditText = requireActivity().findViewById(R.id.code_input)

            val intnt = Intent(requireContext(), JoinRouteActivity::class.java)
            intnt.putExtra("routeId", codeInput.text.toString().replace("#", "").toInt())
            startActivity(intnt)
        }
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        val route = routeList.getJSONObject(markerList.indexOf(p0))

        p0.showInfoWindow()
        val layout: View = requireActivity().findViewById(R.id.data_container)
        layout.visibility = View.VISIBLE

        val routeNameText: TextView = requireActivity().findViewById(R.id.route_name)
        routeNameText.text = route.getString("route_name")
        val celDateText: TextView = requireActivity().findViewById(R.id.celebration_date_text)
        celDateText.text = route.getString("celebration_date").replace("T", " ")

        val btn: Button = requireActivity().findViewById(R.id.detailed_btn)
        btn.setOnClickListener {
            val intnt = Intent(requireContext(), JoinRouteActivity::class.java)
            intnt.putExtra("route", route.toString())
            startActivity(intnt)
        }

        return true
    }

    private fun placeMarker(coord: LatLng, routeName: String) {
        val markerOptions = MarkerOptions().position(coord)
        markerOptions.title(routeName)
        val marker = mMap.addMarker(markerOptions)
        if (marker != null)
            markerList.add(marker)
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_show_routes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val sharedPref : SharedPreferences = requireActivity().getSharedPreferences("userPreference",
            AppCompatActivity.MODE_PRIVATE
        )

        val token = sharedPref.getString("token", "")

        val queue = Volley.newRequestQueue(requireContext())


        val url = "http://10.0.2.2:8000/route/get_routes"

        val jsonObjectRequest = object: JsonArrayRequest(
            Method.GET, url, null,
            { response ->
                routeList = response

                var route: JSONObject
                var points: JSONArray
                var point: JSONObject

                for (i in 0 until routeList.length()) {
                    route = routeList.getJSONObject(i)
                    points = route.getJSONArray("points")

                    for (j in 0 until points.length()) {
                        point = points.getJSONObject(j)
                        if (point.getInt("position") == 0)
                            placeMarker(LatLng(point.getDouble("y_coord"), point.getDouble("x_coord")),
                                route.getString("route_name"))
                    }
                }
            },
            { error ->
                Log.d("error", error.toString())
                Toast.makeText(requireActivity(), requireActivity().getString(R.string.error_get_routes), Toast.LENGTH_SHORT).show()
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