package com.example.chris.ilp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.chris.ilp.R.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var loginButton: Button
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var registerButton: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //set up layout
        email = findViewById<EditText>(id.email)
        password = findViewById<EditText>(id.password)
        loginButton = findViewById<Button>(id.loginButton)
        registerButton = findViewById<Button>(id.registerButton)
        registerButton.setOnClickListener{
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        //create login button listener and check if the email and password is correct.
        loginButton.setOnClickListener{
            if (TextUtils.isEmpty(email.text.toString())) {
                Toast.makeText(applicationContext, "Enter email address!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password.text.toString())) {
                Toast.makeText(applicationContext, "Enter password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email.text).matches()) {
                Toast.makeText(applicationContext, "Please enter a valid email!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance()
            dbRef = database.reference
            auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString()).
                    addOnCompleteListener { task: Task<AuthResult> ->
                        if (!task.isSuccessful) run {
                            // there was an error
                            if (password.length() < 6) {
                                password.error = getString(R.string.minimum_password)
                            } else {
                                Toast.makeText(this@LoginActivity, getString(R.string.auth_failed), Toast.LENGTH_LONG).show()
                            }
                        }
                        else{
                            //update login status and intent to main activity.
                            val userId = auth.currentUser?.uid
                            dbRef.child("users").child(userId.toString()).child("status").setValue("signed_in")
                            val intentToMain = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intentToMain)

                            Toast.makeText(this@LoginActivity,"successful logged in!",Toast.LENGTH_LONG).show()
                        }
                    }
        }
    }

    override fun onBackPressed() {
        Toast.makeText(this@LoginActivity,"Can not return to last activity!!",Toast.LENGTH_LONG).show()
    }
}