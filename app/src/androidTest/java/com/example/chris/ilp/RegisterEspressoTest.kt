package com.example.chris.ilp

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
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
class RegisterEspressoTest {

    private val auth = FirebaseAuth.getInstance()
    @get:Rule
    val activityRule = ActivityTestRule(RegisterActivity::class.java)


    @Before
    fun logout(){
        auth.signInWithEmailAndPassword("test@test.com","123456")
        auth.currentUser?.delete()
    }

    @Test
    fun listGoesOverTheFold() {
        Espresso.onView(ViewMatchers.withId(R.id.displayName)).perform(ViewActions.typeText("for test"))
        Espresso.onView(ViewMatchers.withId(R.id.emailRegister)).perform(ViewActions.typeText("test@test.com"))
        Espresso.onView(ViewMatchers.withId(R.id.passwordRegister)).perform(ViewActions.typeText("123456"))
        Espresso.onView(ViewMatchers.withId(R.id.registerActionButton)).perform(ViewActions.click())
    }


}