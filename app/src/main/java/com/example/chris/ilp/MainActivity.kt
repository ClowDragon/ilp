package com.example.chris.ilp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.example.chris.ilp.R.*
import com.example.chris.ilp.R.id.displayName
import com.google.firebase.auth.FirebaseUser



class MainActivity : AppCompatActivity() {
    private lateinit var displayName: TextView
    private lateinit var logout: Button
    private lateinit var store: Button
    private lateinit var wallet:Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        displayName = findViewById(id.nameTextView) as TextView
        logout = findViewById(id.signoutButton) as Button
        store = findViewById(id.storeButton) as Button
        wallet = findViewById(id.walletButton) as Button

        isLogin()

        logout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun isLogin(){
        val intent = Intent(this@MainActivity, LoginActivity::class.java)

        auth.currentUser?.uid?.let { loadData(it)  } ?: startActivity(intent)

    }

    private fun loadData(userId: String){
        val dataListener = object : ValueEventListener{
        override fun onDataChange(dataSnapshot: DataSnapshot) {
        if(dataSnapshot.exists()){
            var user: User = dataSnapshot.getValue(User::class.java)
            displayName.text = user.displayName
            }
        }

        override fun onCancelled(p0: DatabaseError?) {

        }

        }
        database.reference.child("users").child(userId).addListenerForSingleValueEvent(dataListener)
    }
}