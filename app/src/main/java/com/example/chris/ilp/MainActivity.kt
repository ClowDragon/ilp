package com.example.chris.ilp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
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
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() ,PermissionsListener,LocationEngineListener{

    private lateinit var displayName: TextView
    private lateinit var status:TextView
    private lateinit var uidtext:TextView
    private lateinit var vipicon:ImageView
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

    private val runner = DownloadCompleteRunner
    private val myTask = DownloadFileTask(runner)

    private var userCoins :String = "{\n"+"\"type\":\"FeatureCollection\",\n"+"\"features\":[]\n"+"}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        //set up fire base database.
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbRef = database.reference

        //set up views of layout.
        displayName = findViewById<TextView>(id.name_text)
        uidtext = findViewById<TextView>(id.uidtext)
        status = findViewById<TextView>(id.status_text)
        vipicon = findViewById<ImageView>(id.imageView)
        logout = findViewById<Button>(id.signoutButton)
        store = findViewById<Button>(id.storeButton)
        wallet = findViewById<Button>(id.walletButton)
        collectButton = findViewById<Button>(id.collectButton)
        currentLocationButton = findViewById<Button>(id.currentLocationButton)

        //get the last date of updating map from the preferences file.
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val lastdate = settings.getString("lastDownloadDate","")

        //check if the last data match the current date, if not update map to database using execute(URL)
        if (lastdate != dateInString) {
            myTask.execute(mapURL)
            //create empty local geojson file.
            val file = File(applicationContext.filesDir, "coinzmap" + ".geojson")
            if(!file.exists()){
                file.createNewFile()
            }
        }

        //listener to sign out from current user.
        logout.setOnClickListener {
            val userId = auth.currentUser?.uid
            dbRef.child("users").child(userId.toString()).child("status").setValue("signed_out")
            auth.signOut()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        //get the current user UID and update to screen.
        val testString = "Your UID is : " + auth.currentUser?.uid
        uidtext.text = testString


        //Button for collect the coin
        collectButton.setOnClickListener { _ ->
            //import the user coins value from database
            loadNameAndStatus(auth.currentUser?.uid.toString())
            collectCoin()
            //after collecting the coin we need to update the map to remove the marker.
            map.run {
                mapView.getMapAsync{
                    clear()
                    //update markers
                    addMarkers()
                }
            }
        }

        //intent to store activity.
        store.setOnClickListener {
            val intentToStore = Intent(this@MainActivity,StoreActivity::class.java)
            startActivity(intentToStore)
        }

        //intent to wallet activity.
        wallet.setOnClickListener {
            val intentToWallet = Intent(this@MainActivity,walletActivity::class.java)
            startActivity(intentToWallet)
        }

        //set camera location to current location
        currentLocationButton.setOnClickListener {
            setCameraPosition(originLocation)
        }

        //set up the build in mapbox and add markers to the map.
        Mapbox.getInstance(applicationContext,getString(R.string.access_token))
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { mapboxMap ->
            map = mapboxMap
            enableLocation()
            addMarkers()
        }
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()

        val settings2 = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val lastdate = settings2.getString("lastDownloadDate","")

        if(PermissionsManager.areLocationPermissionsGranted(this)){
            locationEngine?.requestLocationUpdates()
            locationLayerPlugin?.onStart()
        }
        //start the map function.
        mapView.onStart()

        // call is Login function below if we need an update.
        if (lastdate != dateInString) {
            isLogin()
        }

        loadNameAndStatus(auth.currentUser?.uid.toString())

        // use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings2.getString("lastDownloadDate", "")
        // Write a message to ”logcat” (for debugging purposes)
        Log.d(tag, "[onStart] Recalled lastDownloadDate is ’$downloadDate’")

        val editor = settings2.edit()
        editor.putString("lastDownloadDate", dateInString)
        editor.apply()
    }

    //override onStop function to stop the mapView functions.
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


    //function is login to load data from database to local file.
    private fun isLogin(){
        val intent = Intent(this@MainActivity, LoginActivity::class.java)

        auth.currentUser?.uid?.let { loadData(it)  } ?: startActivity(intent)

    }


    //load the data from fire base database using value event listener.
    private fun loadData(userId: String){
        val dataListener = object : ValueEventListener{
        override fun onDataChange(dataSnapshot: DataSnapshot) {
        if(dataSnapshot.exists()){
            //update user data.
            val user: User = dataSnapshot.getValue(User::class.java)!!
            displayName.text = user.displayName
            status.text = user.status

            //write the map saved in database to local file.
            applicationContext.openFileOutput("coinzmap.geojson", Context.MODE_PRIVATE).use {
                it.write(user.map.toByteArray())
            }
            //create a json object in order to get the rates of today and save to database.
            //I'll delete the rates in future operations so i saved rates to database.
            val jsonObject = JSONObject(user.map)
            val rateoftoday = jsonObject.getString("rates")
            dbRef.child("users").child(auth.currentUser?.uid.toString()).child("rates").setValue(rateoftoday)

            //reset user ratio to 1.0 as the booster is expired if date change
            dbRef.child("users").child(auth.currentUser?.uid.toString()).child("ratio").setValue(1.0+user.VIPlevel/2)

            //reset the saving limits to 0 if date change
            dbRef.child("users").child(auth.currentUser?.uid.toString()).child("limit").setValue(0.0)
            }
        }
            override fun onCancelled(error: DatabaseError) { }

        }
        database.reference.child("users").child(userId).addListenerForSingleValueEvent(dataListener)
    }

    //save helper function as above to update name status and userCoins of current user.
    private fun loadNameAndStatus(userId: String){
        val dataListener = object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val user: User = dataSnapshot.getValue(User::class.java)!!
                    displayName.text = user.displayName
                    status.text = user.status
                    userCoins = user.userCoins
                    //update vip icon with user's vip level.
                    when {
                        user.VIPlevel==1 -> vipicon.setImageResource(R.drawable.vip1)
                        user.VIPlevel==2 -> vipicon.setImageResource(R.drawable.vip2)
                        user.VIPlevel==3 -> vipicon.setImageResource(R.drawable.vip3)
                        user.VIPlevel==4 -> vipicon.setImageResource(R.drawable.vip4)
                        else -> {

                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) { }
        }
        database.reference.child("users").child(userId).addListenerForSingleValueEvent(dataListener)
    }

    //helper function for change the date to the format i need.
    private fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }
    //helper function to get current time.
    private fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    //override functions for updating mapView status.
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

    //important helper function to add markers to the map loaded from local file.
    private fun addMarkers(){
        //load coins data from local geojson file.
        val file = File(applicationContext.filesDir, "coinzmap" + ".geojson")
        val geoString = file.readText()
        val geojsonmap = FeatureCollection.fromJson(geoString)
        val fcs = geojsonmap.features()
        /* add marker for each features in feature collection. */
        if (fcs != null) {
            for(fc:Feature in fcs){
                val geometry:Point = fc.geometry() as Point
                val latitude = geometry.latitude()
                val longitude = geometry.longitude()
                val x = LatLng(latitude,longitude)
                //here i load my specific image of marker as a coin.
                val bitmap = BitmapFactory.decodeResource(applicationContext.resources,R.drawable.coin_marker)
                val resizedbitmap = Bitmap.createScaledBitmap(bitmap,100,100,false)
                val coinicon = IconFactory.recreate("coin", resizedbitmap)
                map.addMarker(MarkerOptions().position(x).icon(coinicon))

            }
        }
    }

    //important helper function collect coin
    @SuppressLint("MissingPermission")
    private fun collectCoin(){
        val currentLocation = locationEngine?.lastLocation
        val latitudeOfcurrentPoint = currentLocation?.latitude
        val longitudeOfcurrentPoint = currentLocation?.longitude
        //read file from local json file.
        val file = File(applicationContext.filesDir, "coinzmap" + ".geojson")
        val geoString = file.readText()
        val geojsonmap = FeatureCollection.fromJson(geoString)
        val fcs = geojsonmap.features()
        val newfcs = fcs
        //judge for deciding if the collection is success.
        var judge = false
        if (fcs != null) {
            for(fc in fcs){
                val geometry:Point = fc.geometry() as Point
                val latitudeOfMark = geometry.latitude()
                val longitudeOfMark = geometry.longitude()
                val results = FloatArray(1)
                Location.distanceBetween(latitudeOfcurrentPoint!!.toDouble(), longitudeOfcurrentPoint!!.toDouble(), latitudeOfMark, longitudeOfMark, results)
                val distance = results[0]
                val radius = 25F
                if(distance<radius){
                    //create a new Feature Collection to save the collected coins.
                    val coinmap = FeatureCollection.fromJson(userCoins)
                    val usercoins = coinmap.features()
                    usercoins!!.add(fc)
                    dbRef.child("users").child(auth.currentUser?.uid.toString()).child("userCoins").setValue(FeatureCollection.fromFeatures(usercoins).toJson())

                    //remove the collected feature from grojson file and update the file using a new FeatureCollection.
                    newfcs!!.remove(fc)
                    applicationContext.openFileOutput("coinzmap.geojson", Context.MODE_PRIVATE).use {
                        it.write(FeatureCollection.fromFeatures(newfcs).toJson().toByteArray())
                    }
                    //update judge to see if the collection is success.
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


    //the several basic functions initialise the mapbox.
    private fun enableLocation(){
        if(PermissionsManager.areLocationPermissionsGranted(this)){
            initializeLocationEngine()
            initializeLocationLayer()
        }else{
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(this)
        }
    }

    //initialise mapbox engines.
    @SuppressLint("MissingPermission")
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

    @SuppressLint("MissingPermission")
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

    @SuppressLint("MissingPermission")
    override fun onConnected() {
        locationEngine?.requestLocationUpdates()
    }

}