package com.example.chris.ilp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.example.chris.ilp.R.*
import com.google.firebase.database.*
import com.google.gson.JsonObject
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
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_wallet.*
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() ,PermissionsListener,LocationEngineListener{

    private lateinit var displayName: TextView
    private lateinit var status:TextView
    private lateinit var logout: Button
    private lateinit var store: Button
    private lateinit var wallet:Button
    private lateinit var collectButton: Button
    private lateinit var currentLocationButton: Button

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

    private var userCoins :String = "{\"features\":\"\"}"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbRef = database.reference

        displayName = findViewById(id.name_text) as TextView
        status = findViewById(id.status_text) as TextView
        logout = findViewById(id.signoutButton) as Button
        store = findViewById(id.storeButton) as Button
        wallet = findViewById(id.walletButton) as Button
        collectButton = findViewById(id.collectButton) as Button
        currentLocationButton = findViewById<Button>(id.currentLocationButton)


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

        collectButton.setOnClickListener {
            collectCoin()
            map.run {
                mapView.getMapAsync{
                    clear()
                    //update markers
                    addMarkers()
                }
            }

        }

        wallet.setOnClickListener {
            val intentToWallet = Intent(this@MainActivity,walletActivity::class.java)
            startActivity(intentToWallet)
        }

        currentLocationButton.setOnClickListener {
            setCameraPosition(originLocation)
        }

        Mapbox.getInstance(applicationContext,getString(R.string.access_token))
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            map = mapboxMap
            enableLocation()
            addMarkers()
        }
    }

    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()

        val settings2 = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val lastdate = settings2.getString("lastDownloadDate","")

        if(PermissionsManager.areLocationPermissionsGranted(this)){
            locationEngine?.requestLocationUpdates()
            locationLayerPlugin?.onStart()
        }
        mapView.onStart()
        // Restore preferences
        if (!lastdate.equals(dateInString)) {
            isLogin()
        }

        if (!lastdate.equals(dateInString)) {
            val file = File(applicationContext.filesDir, "coinzmap" + ".geojson")
            val geoString = file.readText()
            val jsonObject = JSONObject(geoString)
            val rateoftoday = jsonObject.getString("rates")
            dbRef.child("users").child(auth.currentUser?.uid).child("rates").setValue(rateoftoday)
        }

        // use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings2.getString("lastDownloadDate", "")
        // Write a message to ”logcat” (for debugging purposes)
        Log.d(tag, "[onStart] Recalled lastDownloadDate is ’$downloadDate’")

        val editor = settings2.edit()
        editor.putString("lastDownloadDate", dateInString)

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

    private fun loadUserCoins(userId: String){
        val dataListener = object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val user: User = dataSnapshot.getValue(User::class.java)
                     userCoins = user.userCoins

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
                val bitmap = BitmapFactory.decodeResource(applicationContext.resources,R.drawable.coin_marker)
                val resized_bitmap = Bitmap.createScaledBitmap(bitmap,100,100,false)
                val coinicon = IconFactory.recreate("coin", resized_bitmap)
                map.addMarker(MarkerOptions().position(x).icon(coinicon))

            }
        }
    }


    private fun collectCoin(){
        val currentLocation = locationEngine?.lastLocation
        val latitudeOfcurrentPoint = currentLocation?.latitude
        val longitudeOfcurrentPoint = currentLocation?.longitude

        val file = File(applicationContext.filesDir, "coinzmap" + ".geojson")
        val geoString = file.readText()
        val geojsonmap = FeatureCollection.fromJson(geoString)
        val fcs = geojsonmap.features()
        var newfcs = fcs
        var judge :Boolean= false
        if (fcs != null) {
            for(fc in fcs){
                val geometry:Point = fc.geometry() as Point
                val latitudeOfMark = geometry.latitude()
                val longitudeOfMark = geometry.longitude()
                val results = FloatArray(1)
                Location.distanceBetween(latitudeOfcurrentPoint!!.toDouble(), longitudeOfcurrentPoint!!.toDouble(), latitudeOfMark, longitudeOfMark, results)
                val distance = results[0]
                val radius:Float = 25F
                if(distance<radius){
                    //addCoinToWallet(fc)
                    newfcs!!.remove(fc)
                    applicationContext.openFileOutput("coinzmap.geojson", Context.MODE_PRIVATE).use {
                        it.write(FeatureCollection.fromFeatures(newfcs).toJson().toByteArray())
                    }

                    loadUserCoins(auth.currentUser?.uid.toString())
                    val geoCoins = JSONObject(userCoins)
                    val temp:String = geoCoins.get("features").toString()
                    geoCoins.put("features",temp+fc.properties().toString())
                    dbRef.child("users").child(auth.currentUser?.uid).child("userCoins").setValue(geoCoins.toString())
                    //测试的时候删掉别update到firebase
                    //dbRef.child("users").child(auth.currentUser?.uid).child("map").setValue(FeatureCollection.fromFeatures(newfcs).toJson())

                    judge = true
                    break
                }
            }
        }
        else{
            Toast.makeText(this@MainActivity,"No coins left!",Toast.LENGTH_LONG).show()
        }

        if (!judge){
            Toast.makeText(this@MainActivity,"Fail to Collect!",Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(this@MainActivity,"You get a coin!",Toast.LENGTH_LONG).show()
        }
    }

    private fun addCoinToWallet(fc:Feature){
        val textView = TextView(this)
        textView.height = 30
        textView.width = LayoutParams.MATCH_PARENT
        textView.text = "Coin Type:"+ fc.properties()?.get("currency") +"   value:" + fc.properties()?.get("value")
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,30F)
        textView.setTextColor(Color.BLACK)
        walletLayout.addView(textView)
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
        locationEngine?.addLocationEngineListener(this)
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
                LatLng(location.latitude,location.longitude),15.0))
    }


    override fun onExplanationNeeded(p0: MutableList<String>?) {
        //present a dialog explain why they need to grant access
        Toast.makeText(this@MainActivity,"we need permission to access your location!",Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if(granted){
            enableLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionManager.onRequestPermissionsResult(requestCode,permissions,grantResults)
    }


    override fun onLocationChanged(location: Location?) {
        location?.let {
            originLocation = location
            setCameraPosition(location)
        }
    }

    override fun onConnected() {
        locationEngine?.requestLocationUpdates()
    }

}