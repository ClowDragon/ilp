package com.example.chris.ilp

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

private var userLimit = 24.0

@RunWith(AndroidJUnit4::class)
class AssertLimitTest {
    @Test
    @Throws(Exception::class)
    fun checkGold() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        Assert.assertEquals("com.example.chris.ilp", appContext.packageName)

        val user = User()
        user.limit = userLimit

        Assert.assertTrue(24.0==userLimit)
    }
}