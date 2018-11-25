package com.example.chris.ilp

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.example.chris.ilp.R.*
import com.google.firebase.database.*
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() ,PermissionsListener,LocationEngineListener{

    private lateinit var displayName: TextView
    private lateinit var status:TextView
    private lateinit var logout: Button
    private lateinit var store: Button
    private lateinit var wallet:Button

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference

    private lateinit var mapView: MapView
    private lateinit var map:MapboxMap
    private lateinit var permissionManager:PermissionsManager
    private lateinit var originLocation: Location

    private var locationEngine : LocationEngine?=null
    private var locationLayerPlugin : LocationLayerPlugin?=null

    private var downloadDate = "" // Format: YYYY/MM/DD
    private val preferencesFile = "MyPrefsFile" // for storing preferences
    private val tag = "MainActivity"

    private val date = getCurrentDateTime()
    private val dateInString = date.toString("yyyy/MM/dd")
    private val mapURL = "http://homepages.inf.ed.ac.uk/stg/coinz/"+dateInString+"/coinzmap.geojson"

    val runner = DownloadCompleteRunner
    val myTask = DownloadFileTask(runner)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        Toast.makeText(this@MainActivity,"successful logged in!",Toast.LENGTH_LONG).show()

        Mapbox.getInstance(applicationContext,getString(R.string.access_token))
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            map = mapboxMap
            enableLocation()
            addMarkers()
        }




        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbRef = database.reference

        displayName = findViewById(id.name_text) as TextView
        status = findViewById(id.status_text) as TextView
        logout = findViewById(id.signoutButton) as Button
        store = findViewById(id.storeButton) as Button
        wallet = findViewById(id.walletButton) as Button


        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val lastdate = settings.getString("lastDownloadDate","")
        if (!lastdate.equals(dateInString)) {
            myTask.execute(mapURL)
            val file = File(applicationContext.filesDir, "coinzmap" + ".geojson")
            if(!file.exists()){
                file.createNewFile()
            }
        }

        logout.setOnClickListener {
            val userId = auth.currentUser?.uid
            dbRef.child("users").child(userId).child("status").setValue("signed_out")
            auth.signOut()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()
        if(PermissionsManager.areLocationPermissionsGranted(this)){
            locationEngine?.requestLocationUpdates()
            locationLayerPlugin?.onStart()
        }
        mapView.onStart()
        // Restore preferences
        isLogin()

        //myTask.execute(mapURL)


        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "")
        // Write a message to ”logcat” (for debugging purposes)
        Log.d(tag, "[onStart] Recalled lastDownloadDate is ’$downloadDate’")

    }

    override fun onStop() {
        super.onStop()
        locationEngine?.removeLocationUpdates()
        locationLayerPlugin?.onStop()
        mapView.onStop()
        downloadDate = dateInString
        Log.d(tag, "[onStop] Storing lastDownloadDate of $downloadDate")
        // All objects are from android.context.Context
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // We need an Editor object to make preference changes.
        val editor = settings.edit()
        editor.putString("lastDownloadDate", downloadDate)
        // Apply the edits!
        editor.apply()

    }



    private fun isLogin(){
        val intent = Intent(this@MainActivity, LoginActivity::class.java)

        auth.currentUser?.uid?.let { loadData(it)  } ?: startActivity(intent)

    }

    private fun loadData(userId: String){
        val dataListener = object : ValueEventListener{
        override fun onDataChange(dataSnapshot: DataSnapshot) {
        if(dataSnapshot.exists()){
            val user: User = dataSnapshot.getValue(User::class.java)
            displayName.text = user.displayName
            status.text = user.status
            applicationContext.openFileOutput("coinzmap.geojson", Context.MODE_PRIVATE).use {
                it.write(user.map.toByteArray())
            }

            }
        }

        override fun onCancelled(p0: DatabaseError?) {

        }

        }
        database.reference.child("users").child(userId).addListenerForSingleValueEvent(dataListener)
    }

    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        locationEngine?.deactivate()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        if(outState!=null){
            mapView.onSaveInstanceState(outState)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun addMarkers(){
        val file = File(applicationContext.filesDir, "coinzmap" + ".geojson")
        val geoString = file.readText()
        val geojsonmap = FeatureCollection.fromJson(geoString)
        val fcs = geojsonmap.features()
        if (fcs != null) {
            for(fc:Feature in fcs){
                val geometry:Point = fc.geometry() as Point
                val latitude = geometry.latitude()
                val longitude = geometry.longitude()
                val x = LatLng(latitude,longitude)
                map.addMarker(MarkerOptions().position(x))
            }
        }
    }


    private fun enableLocation(){
        if(PermissionsManager.areLocationPermissionsGranted(this)){
            initializeLocationEngine()
            initializeLocationLayer()
        }else{
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(this)
        }
    }

    private fun initializeLocationEngine(){
        locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
        locationEngine?.priority = LocationEnginePriority.HIGH_ACCURACY
        locationEngine?.activate()

        val lastLocation = locationEngine?.lastLocation
        if(lastLocation!=null){
            originLocation = lastLocation
            setCameraPosition(lastLocation)
        }else{
            locationEngine?.addLocationEngineListener(this)
        }
    }

    private fun initializeLocationLayer(){
        locationLayerPlugin = LocationLayerPlugin(mapView,map,locationEngine)
        locationLayerPlugin?.setLocationLayerEnabled(true)
        locationLayerPlugin?.cameraMode = CameraMode.TRACKING
        locationLayerPlugin?.renderMode = RenderMode.NORMAL
    }

    private fun setCameraPosition(location: Location){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(location.latitude,location.longitude),13.0))
    }



    override fun onExplanationNeeded(p0: MutableList<String>?) {
        //present a dialog explain why they need to grant access
        Toast.makeText(this@MainActivity,"we need permission to access your location!",Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(p0: Boolean) {
        if(p0){
            enableLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionManager.onRequestPermissionsResult(requestCode,permissions,grantResults)
    }


    override fun onLocationChanged(location: Location?) {
        location?.let{
            originLocation = location
            setCameraPosition(location)
        }
    }

    override fun onConnected() {
        locationEngine?.requestLocationUpdates()
    }
}