package com.example.chris.ilp

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.example.chris.ilp.R.*
import com.google.firebase.database.*
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var displayName: TextView
    private lateinit var status:TextView
    private lateinit var logout: Button
    private lateinit var store: Button
    private lateinit var wallet:Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private lateinit var mapView: MapView
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

        Mapbox.getInstance(applicationContext,getString(R.string.access_token))
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)


        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbRef = database.reference

        displayName = findViewById(id.name_text) as TextView
        status = findViewById(id.status_text) as TextView
        logout = findViewById(id.signoutButton) as Button
        store = findViewById(id.storeButton) as Button
        wallet = findViewById(id.walletButton) as Button

        myTask.execute(mapURL)


        logout.setOnClickListener {
            val userId = auth.currentUser?.uid
            dbRef.child("users").child(userId).child("status").setValue("signed_out")
            auth.signOut()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    override fun onStart() {
        super.onStart()
        // Restore preferences
        isLogin()

        //myTask.execute(mapURL)
        val file = File(applicationContext.filesDir, "coinzmap" + ".geojson")
        file.createNewFile()


        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate", "")
        // Write a message to ”logcat” (for debugging purposes)
        Log.d(tag, "[onStart] Recalled lastDownloadDate is ’$downloadDate’")



    }


    override fun onStop() {
        super.onStop()
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

}