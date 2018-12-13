package com.example.chris.ilp

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

private var VIPlevel = 4

@RunWith(AndroidJUnit4::class)
class AssertLevelTest {
    @Test
    @Throws(Exception::class)
    fun checkGold() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        Assert.assertEquals("com.example.chris.ilp", appContext.packageName)

        val user = User()
        user.VIPlevel = VIPlevel

        Assert.assertTrue(4==user.VIPlevel)
    }
}