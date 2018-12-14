package com.example.chris.ilp

import android.support.test.runner.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssertRegisterSuccessTest {
    private lateinit var auth: FirebaseAuth
    private val email = "test@test.com"
    private val password = "123456"
    @Before
    fun deleteAccount(){
        auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email,password)
        auth.currentUser?.delete()
    }

    @Test
    @Throws(Exception::class)
    fun useAppContext() {
        auth = FirebaseAuth.getInstance()
        var result=false
        auth.createUserWithEmailAndPassword(email,password)
        auth.signInWithEmailAndPassword(email,password)
        if (auth.currentUser!=null){
            result = true
        }
        //i have created this account in database so should return false.
        Assert.assertEquals(true, result)
    }

}