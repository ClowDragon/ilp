package com.example.chris.ilp

import android.support.test.runner.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssertSignedInTest {
    @Test
    @Throws(Exception::class)
    fun useAppContext() {
        val auth = FirebaseAuth.getInstance()
        val email = "rin@123.com"
        val password = "123456"
        auth.signInWithEmailAndPassword(email,password)
        Assert.assertNotEquals(null,auth.currentUser)
    }
}