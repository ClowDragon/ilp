package com.example.chris.ilp

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
private var userGold = 100.0

@RunWith(AndroidJUnit4::class)
class AssertGoldTest {
    @Test
    @Throws(Exception::class)
    fun checkGold() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        Assert.assertEquals("com.example.chris.ilp", appContext.packageName)

        val user = User()
        user.gold = userGold

        Assert.assertTrue(100.0==user.gold)
    }
}
