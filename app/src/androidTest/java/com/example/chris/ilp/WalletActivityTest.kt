package com.example.chris.ilp

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class WalletActivityTest {
    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)
    //run this test after loginEspressoTest to make sure it is successful occasionally goes wrong
    //as the data is not loaded to text yet
    @Before
    fun signIn(){
        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword("test@123.com","123456")
    }

    @Test
    fun walletActivityDisplay() {
        onView(ViewMatchers.withId(R.id.walletButton)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.textView)).check(ViewAssertions.matches(ViewMatchers.withText("Your Coins")))
    }

    @After
    fun signOut(){
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
    }
}