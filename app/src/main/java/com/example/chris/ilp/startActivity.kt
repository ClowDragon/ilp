package com.example.chris.ilp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class startActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        var auth = FirebaseAuth.getInstance()
        var database = FirebaseDatabase.getInstance()
        var dbRef = database.reference
        val userId = auth.currentUser?.uid
        isLogin(userId.toString())
        super.onCreate(savedInstanceState)
            
        }

    fun isLogin(userId: String){
        var auth = FirebaseAuth.getInstance()
        var database = FirebaseDatabase.getInstance()
        var dbRef = database.reference
        val dataListener = object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot){
                if(dataSnapshot.exists()){
                    val user: User = dataSnapshot.getValue(User::class.java)
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

            override fun onCancelled(p0: DatabaseError?) {

            }

        }
        database.reference.child("users").child(userId).addListenerForSingleValueEvent(dataListener)
    }

}