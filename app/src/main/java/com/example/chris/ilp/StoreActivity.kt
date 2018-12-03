package com.example.chris.ilp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StoreActivity: AppCompatActivity(){


    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private lateinit var usergold : TextView
    private lateinit var userCurrentRatio:TextView
    private lateinit var goldboosterbutton :Button
    private lateinit var backtogameimageButton: ImageButton
    private var userRatio = 1.0
    private var userGold = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbRef = database.reference

        usergold = findViewById<TextView>(R.id.usergold)
        userCurrentRatio = findViewById<TextView>(R.id.userCurrentRatio)
        goldboosterbutton = findViewById<Button>(R.id.goldboosterbutton)
        backtogameimageButton = findViewById<ImageButton>(R.id.backtogameimageButton)

        loadUserRatio(auth.currentUser?.uid.toString())

        backtogameimageButton.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        goldboosterbutton.setOnClickListener {
            if(userGold<1000){
                Toast.makeText(this@StoreActivity,"Not enough gold!",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            else{
                Toast.makeText(this@StoreActivity,"You get a gold booster!!",Toast.LENGTH_LONG).show()
                dbRef.child("users").child(auth.currentUser?.uid.toString()).child("ratio").setValue(userRatio*2)
                dbRef.child("users").child(auth.currentUser?.uid.toString()).child("gold").setValue(userGold-1000)
                loadUserRatio(auth.currentUser?.uid.toString())
            }
        }
    }


    private fun loadUserRatio(userId: String){
        val dataListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val user: User = dataSnapshot.getValue(User::class.java)!!
                    userRatio = user.ratio
                    userGold = user.gold
                    val textofgold = "Your gold: "+user.gold.toString()
                    usergold.text = textofgold
                    val textofratio = "Your exchange ratio is : " + user.ratio.toString()
                    userCurrentRatio.text = textofratio
                }
            }
            override fun onCancelled(error: DatabaseError) { }
        }
        database.reference.child("users").child(userId).addListenerForSingleValueEvent(dataListener)
    }

}