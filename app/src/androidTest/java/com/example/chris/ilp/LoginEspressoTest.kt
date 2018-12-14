package com.example.chris.ilp

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
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
class LoginEspressoTest {
    @get:Rule
    val activityRule = ActivityTestRule(LoginActivity::class.java)

    @Test
    fun typeLogInInformation() {
        Espresso.onView(ViewMatchers.withId(R.id.email)).perform(typeText("test@123.com"))
        Espresso.onView(ViewMatchers.withId(R.id.password)).perform(typeText("123456"))
        Espresso.onView(ViewMatchers.withId(R.id.loginButton)).perform(click())
    }
}