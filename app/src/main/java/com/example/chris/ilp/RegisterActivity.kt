package com.example.chris.ilp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.text.TextUtils
import android.util.Patterns
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var displayName: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var registerButton: Button
    private lateinit var auth:FirebaseAuth
    private lateinit var database:FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private lateinit var signInButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //set up fire base database.
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbRef = database.reference

        //set up views from layout.
        displayName = findViewById<EditText>(R.id.displayName)
        email = findViewById<EditText>(R.id.emailRegister)
        password = findViewById<EditText>(R.id.passwordRegister)
        registerButton = findViewById<Button>(R.id.registerActionButton)
        signInButton = findViewById<Button>(R.id.sign_in_button)

        //sign in listener for intent back to login activity.
        signInButton.setOnClickListener{
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        //register using the email and password from edit text and check conditions.
        registerButton.setOnClickListener {
            if (TextUtils.isEmpty(displayName.text.toString())) {
                Toast.makeText(applicationContext, "Enter your name!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(email.text.toString())) {
                Toast.makeText(applicationContext, "Enter email address!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password.text.toString())) {
                Toast.makeText(applicationContext, "Enter password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length() < 6) {
                Toast.makeText(applicationContext, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //check the email format.
            if (!Patterns.EMAIL_ADDRESS.matcher(email.text).matches()) {
                Toast.makeText(applicationContext, "Please enter a valid email!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //create the account and save it to database.
            else{
                auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString()).
                    addOnCompleteListener { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            Toast.makeText(applicationContext, "Account Created!", Toast.LENGTH_SHORT).show()
                            val userId = auth.currentUser?.uid
                            val user = User(displayName.text.toString(),"signed_out","","","{\n"+"\"type\":\"FeatureCollection\",\n"+"\"features\":[]\n"+"}",0.0,1.0,0.0,"{\n"+"\"type\":\"FeatureCollection\",\n"+"\"features\":[]\n"+"}",0)
                            dbRef.child("users").child(userId.toString()).setValue(user)
                            val intentToLoginActivity = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intentToLoginActivity)
                        }
                    }
            }
        }
    }
}
