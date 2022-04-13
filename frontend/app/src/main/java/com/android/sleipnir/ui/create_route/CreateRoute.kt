package com.android.sleipnir.ui.create_route

import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.sleipnir.R
import com.google.android.gms.location.FusedLocationProviderClient

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import com.android.sleipnir.DrawerActivity
import com.android.sleipnir.FillRouteInfoActivity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import java.util.ArrayList

class CreateRoute : Fragment(), GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap

    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var btn: Button

    private var markerList = ArrayList<Marker>()

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(this)

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

    override fun onMarkerClick(p0: Marker) = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_route, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        btn = requireActivity().findViewById(R.id.introduce_info_btn)
        btn.setOnClickListener {

            val pointList = ArrayList<LatLng>()

            for (marker in markerList) {
                pointList.add(marker.position)
            }

            val userId = activity?.intent?.getIntExtra("userId", -1)
            val token = activity?.intent?.getStringExtra("token")
            val userName = activity?.intent?.getStringExtra("userName")

            val intnt = Intent(requireContext(), FillRouteInfoActivity::class.java)

            intnt.putParcelableArrayListExtra("points", pointList)
            intnt.putExtra("userId", userId)
            intnt.putExtra("token", token)
            intnt.putExtra("userName", userName)
            startActivity(intnt)
        }
    }

    override fun onMapClick(p0: LatLng) {
        val markerOptions = MarkerOptions().position(p0)
        markerOptions.draggable(true)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
        if (markerList.isEmpty()) {
            btn.visibility = View.VISIBLE
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        }
        val marker: Marker? = mMap.addMarker((markerOptions))
        if (marker != null) {
            markerList.add(marker)
        }
    }


}