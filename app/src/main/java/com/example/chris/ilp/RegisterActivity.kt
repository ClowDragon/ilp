package com.example.chris.ilp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.text.TextUtils
import com.google.android.gms.common.SignInButton
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

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

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        dbRef = database.reference

        displayName = findViewById(R.id.displayName) as EditText
        email = findViewById(R.id.emailRegister) as EditText
        password = findViewById(R.id.passwordRegister) as EditText
        registerButton = findViewById(R.id.registerActionButton) as Button
        signInButton = findViewById(R.id.sign_in_button) as Button

        signInButton.setOnClickListener{
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }


        registerButton.setOnClickListener(){
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

            else{
                auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString()).
                    addOnCompleteListener { task: Task<AuthResult> ->
                        if (task.isSuccessful) {
                            Toast.makeText(applicationContext, "Account Created!", Toast.LENGTH_SHORT).show()
                            val userId = auth.currentUser?.uid
                            val user = User(displayName.text.toString())
                            dbRef.child("users").child(userId).setValue(user)
                            val intentToLoginActivity = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intentToLoginActivity)
                        }
                    }
            }
        }
    }
}
