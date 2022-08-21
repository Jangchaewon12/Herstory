package com.example.woman_project

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
        private lateinit var mMap: GoogleMap
        // 현재 위치를 검색하기 위함
        private lateinit var fusedLocationClient: FusedLocationProviderClient // 위칫값 사용
        private lateinit var locationCallback: LocationCallback // 위칫값 요청에 대한 갱신 정보를 받아옴
        //geofence
        lateinit var geofencingClient: GeofencingClient
        val geofenceList: MutableList<Geofence> by lazy{
            mutableListOf()
        }

        override fun onCreate(savedInstanceState: Bundle?) {

            setContentView(R.layout.activity_maps)

            // 사용할 권한 array로 저장
            val permissions = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)

            requirePermissions(permissions, 999) //권한 요청, 999는 임의의 숫자
        }

        fun startProcess() {
            // SupportMapFragment를 가져와서 지도가 준비되면 알림을 받습니다.
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }

        /** 권한 요청*/
        fun requirePermissions(permissions: Array<String>, requestCode: Int) {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                permissionGranted(requestCode)
            } else {
                val isAllPermissionsGranted = permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
                if (isAllPermissionsGranted) {
                    permissionGranted(requestCode)
                } else {
                    ActivityCompat.requestPermissions(this, permissions, requestCode)
                }
            }
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)//!논란의 여지!
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                permissionGranted(requestCode)
            } else {
                permissionDenied(requestCode)
            }
        }

        // 권한이 있는 경우 실행
        fun permissionGranted(requestCode: Int) {
            startProcess() // 권한이 있는 경우 구글 지도를준비하는 코드 실행
        }

        // 권한이 없는 경우 실행
        fun permissionDenied(requestCode: Int) {
            Toast.makeText(this
                , "권한 승인이 필요합니다."
                , Toast.LENGTH_LONG)
                .show()
        }

        override fun onMapReady(googleMap: GoogleMap) {
            mMap = googleMap
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            updateLocation()
        }

        // 위치 정보를 받아오는 역할
        @SuppressLint("MissingPermission") //requestLocationUpdates는 권한 처리가 필요한데 현재 코드에서는 확인 할 수 없음. 따라서 해당 코드를 체크하지 않아도 됨.
        fun updateLocation() {
            val locationRequest = LocationRequest.create()
            locationRequest.run {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 1000
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult?.let {
                        for(location in it.locations) {
                            Log.d("Location", "${location.latitude} , ${location.longitude}")
                            setLastLocation(location)
                        }
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }

        fun setLastLocation(lastLocation: Location) {
            val LATLNG = LatLng(lastLocation.latitude, lastLocation.longitude)
            val markerOptions = MarkerOptions()
                .position(LATLNG)
                .title("Here!")

            val cameraPosition = CameraPosition.Builder()
                .target(LATLNG)
                .zoom(15.0f)
                .build()
            mMap.clear()
            mMap.addMarker(markerOptions)
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }


    private fun getGeofence(reqId: String, geo: LatLng, radius: Float): Geofence {
        return Geofence.Builder()
            .setRequestId(reqId)
            .setCircularRegion(geo.latitude, geo.longitude, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setLoiteringDelay(10000)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER
            )
            .build()
    }

    val geoPending: PendingIntent by lazy{
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun addGeofences(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED){
            geofencingClient.addGeofences(getGeofencingRequest(geofenceList), geoPending).run{
                addOnSuccessListener {
                    Log.e("addGeo", "add Success")
                }
                addOnFailureListener {
                    Log.e("addGeo", "add Fail")

                }
            }
        }
    }


    private fun getGeofencingRequest(list: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply{
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(list)
        }.build()
    }

}

    //geofence


    //LatLng DEFAULT_LOCATION = new LatLng(37.56,126.97);

