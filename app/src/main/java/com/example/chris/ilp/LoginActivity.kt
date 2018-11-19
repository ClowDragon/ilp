package com.example.chris.ilp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.chris.ilp.R.*
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var loginButton: Button
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        email = findViewById(id.email) as EditText
        password = findViewById(id.password) as EditText
        loginButton = findViewById(id.loginButton) as Button
        registerButton = findViewById(id.registerButton) as Button
        registerButton.setOnClickListener{
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
        loginButton.setOnClickListener{
            if (TextUtils.isEmpty(email.text.toString())) {
                Toast.makeText(applicationContext, "Enter email address!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(password.text.toString())) {
                Toast.makeText(applicationContext, "Enter password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth = FirebaseAuth.getInstance()
            auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString()).
                    addOnCompleteListener { task: Task<AuthResult> ->
                        if (!task.isSuccessful) run {
                            // there was an error
                            if (password.length() < 6) {
                                password.setError(getString(R.string.minimum_password))
                            } else {
                                Toast.makeText(this@LoginActivity, getString(R.string.auth_failed), Toast.LENGTH_LONG).show()
                            }
                        }
                        else{
                            val intentToMain = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intentToMain)
                        }
                    }
        }
    }
}