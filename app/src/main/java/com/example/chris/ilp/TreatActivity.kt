package com.example.chris.ilp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mapbox.geojson.FeatureCollection
import kotlinx.android.synthetic.main.activity_login.*

class TreatActivity:AppCompatActivity(){

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference

    private lateinit var treatingcoin : TextView
    private lateinit var emailfortreat : EditText
    private lateinit var savetobank : Button
    private lateinit var sendtoothers : Button
    private lateinit var returntowallet : Button
    private var userCoins :String = ""
    private var index : Int = 0

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
            //The key argument here must match that used in the other activity
        }

        loadCoin(auth.currentUser?.uid.toString())

        returntowallet.setOnClickListener {
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

}