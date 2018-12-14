package com.example.chris.ilp

import android.support.test.runner.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterFailTest {
    @Test
    @Throws(Exception::class)
    fun useAppContext() {
        val auth = FirebaseAuth.getInstance()
        val email = "test@123.com"
        val password = "123456"
        var result = false
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{
            if (it.isSuccessful){
                result = true
            }
        }
        //i have created this account in database so should return false.
        assertEquals(false,result)
    }
}