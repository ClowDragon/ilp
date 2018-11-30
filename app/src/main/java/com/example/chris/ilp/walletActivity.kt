package com.example.chris.ilp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.example.chris.ilp.R.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_wallet.*

class walletActivity:AppCompatActivity(){
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private lateinit var backToGame:Button
    private lateinit var rateOfToday:TextView
    private var rates = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_wallet)
        backToGame = findViewById(id.backtogameButton) as Button
        rateOfToday = findViewById(id.rateOfToday) as TextView

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbRef = database.reference

        loadData(auth.currentUser?.uid.toString())

        backToGame.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadData(userId: String){
        val dataListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val user: User = dataSnapshot.getValue(User::class.java)
                    rates = user.rates
                    var textRates = user.rates
                    rateOfToday.text = "Today's Exchange Rates: \n"+ textRates.split(",")[0].drop(1) +"\n"+textRates.split(",")[1]+ "\n" +textRates.split(",")[2] +"\n" +textRates.split(",")[3].dropLast(1)
                }
            }

            override fun onCancelled(p0: DatabaseError?) {

            }

        }
        database.reference.child("users").child(userId).addListenerForSingleValueEvent(dataListener)
    }
}