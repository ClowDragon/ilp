package com.example.chris.ilp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.delay

class startActivity : AppCompatActivity() {

    private lateinit var startbutton:Button
    private lateinit var auth:FirebaseAuth
    private lateinit var database:FirebaseDatabase

    //Start activity to check the status of current user thus intent to main or login activity.
    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid
        isLogin(userId.toString())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acticity_start)
        suspend {  delay(timeMillis = 2000) }
        startbutton = findViewById<Button>(R.id.startingbutton)

        startbutton.setOnClickListener {
            val intenttologin = Intent(this@startActivity,LoginActivity::class.java)
            startActivity(intenttologin)
        }

    }

    //helper function to check if the user status is logged in or logged out.
    private fun isLogin(userId: String){
        database = FirebaseDatabase.getInstance()
        val dataListener = object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                if(dataSnapshot.exists()){
                    val user: User = dataSnapshot.getValue(User::class.java)!!
                    if(user.status.equals("signed_in")){
                        val intentToMain = Intent(this@startActivity,MainActivity::class.java)
                        startActivity(intentToMain)
                        finish()
                    }
                    else{
                        val intentToLogin = Intent(this@startActivity,LoginActivity::class.java)
                        startActivity(intentToLogin)
                        finish()
                    }
                }
                else{
                    val intentToLogin = Intent(this@startActivity,LoginActivity::class.java)
                    startActivity(intentToLogin)
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) { }

        }
        database.reference.child("users").child(userId).addListenerForSingleValueEvent(dataListener)
    }

}