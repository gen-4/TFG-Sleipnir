package com.android.sleipnir.ui.check_observeds

import android.Manifest
import android.app.Activity
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.sleipnir.R
import com.android.sleipnir.ui.show_routes.ShowRoutes
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

class ShowObserversUbicationFragment : Fragment(), GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap

    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var markerList = ArrayList<Marker>()


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
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        p0.showInfoWindow()
        return true
    }

    private fun placeMarker(coord: LatLng, rider: String) {
        val markerOptions = MarkerOptions().position(coord)
        markerOptions.title(rider)
        val marker = mMap.addMarker(markerOptions)
        if (marker != null)
            markerList.add(marker)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_show_observers_ubication, container, false)
    }

    private fun updateUbications() {
        mMap.clear()
        markerList.clear()

        val sharedPref : SharedPreferences = requireActivity().getSharedPreferences("userPreference",
            AppCompatActivity.MODE_PRIVATE
        )

        val token = sharedPref.getString("token", "")
        val userId = sharedPref.getInt("userId", -1)

        val queue = Volley.newRequestQueue(requireContext())

        val url = "http://10.0.2.2:8000/user/".plus(userId.toString()).plus("/observeds")

        val jsonObjectRequest = object: JsonArrayRequest(
            Method.GET, url, null,
            { response ->

                var point: JSONObject

                for (i in 0 until response.length()) {
                    point = response.getJSONObject(i)
                    placeMarker(LatLng(point.getDouble("last_y_coord"), point.getDouble("last_x_coord")),
                        point.getJSONObject("user").getString("username"))
                }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        updateUbications()

        val btn: Button = requireActivity().findViewById(R.id.refresh_btn)
        btn.setOnClickListener { updateUbications() }

    }
}