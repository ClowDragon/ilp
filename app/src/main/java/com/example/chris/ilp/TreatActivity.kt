package com.example.chris.ilp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONArray
import org.json.JSONObject

class TreatActivity:AppCompatActivity(){

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference

    private lateinit var treatingcoin : TextView
    private lateinit var emailfortreat : EditText
    private lateinit var savetobank : Button
    private lateinit var sendtoothers : Button
    private lateinit var returntowallet : Button
    private var userCoins :String = "for saving user coins"
    private var ratio : Double = 1.0
    private var index : Int = 0
    private var rates : String = ""
    private var usergold : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_treat)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbRef = database.reference

        treatingcoin = findViewById<TextView>(R.id.treatingcoin)
        emailfortreat = findViewById<EditText>(R.id.emailfortreat)
        savetobank = findViewById<Button>(R.id.savetobank)
        sendtoothers = findViewById<Button>(R.id.sendtoother)
        returntowallet = findViewById<Button>(R.id.returntowallet)

        val extras = intent.extras
        if (extras != null) {
            index = extras.getInt("key")
            //The key argument here must match that used in the wallet activity
        }

        loadCoin(auth.currentUser?.uid.toString())

        returntowallet.setOnClickListener {
            val intenttowallet = Intent(this@TreatActivity,walletActivity::class.java)
            startActivity(intenttowallet)
        }

        savetobank.setOnClickListener {
            saveToBank()
            Toast.makeText(this@TreatActivity,"Saved to Bank!",Toast.LENGTH_LONG).show()
            val intenttowallet = Intent(this@TreatActivity,walletActivity::class.java)
            startActivity(intenttowallet)
        }
    }


    private fun loadCoin(userId: String){
        val dataListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val user: User = dataSnapshot.getValue(User::class.java)!!
                    userCoins = user.userCoins
                    rates = user.rates
                    usergold = user.gold
                    val geoCoins = FeatureCollection.fromJson(user.userCoins)
                    val coins = geoCoins.features()
                    val targetfeature = coins!!.get(index)
                    val text1 = targetfeature.properties()!!.get("currency").toString()
                    val text2 = targetfeature.properties()!!.get("value").toString()
                    treatingcoin.text = "Coin type: "+text1 + "\n" + "Coin value: " + text2
                }
            }
            override fun onCancelled(error: DatabaseError) { }
        }
        database.reference.child("users").child(userId).addListenerForSingleValueEvent(dataListener)
    }

    private fun saveToBank(){
        val geoCoins = FeatureCollection.fromJson(this.userCoins)
        val coins = geoCoins.features()
        val targetcoin = coins!!.get(index)
        val value = targetcoin.properties()!!.get("value").asDouble
        val type = targetcoin.properties()!!.get("currency").asString

        val ratesJson = JSONObject(rates)
        val thisrate = ratesJson.get(type).toString().toDouble()
        val gold = value * thisrate * ratio + usergold
        dbRef.child("users").child(auth.currentUser?.uid.toString()).child("gold").setValue(gold)

        coins.removeAt(index)
        val newuserCoins = FeatureCollection.fromFeatures(coins)
        dbRef.child("users").child(auth.currentUser?.uid.toString()).child("userCoins").setValue(newuserCoins.toJson())
    }

}