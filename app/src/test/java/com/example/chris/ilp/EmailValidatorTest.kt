package com.example.chris.ilp

import com.google.common.truth.Truth
import org.junit.Test
import java.util.regex.Pattern

//since all data are saved in database, I only write the email test to test the format.
val email_pattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
)!!

class EmailValidatorTest {
    @Test
    @Throws(Exception::class)
    fun emailValidator_CorrectEmailSimple_ReturnsTrue() {
        val email = "s1688223@sms.ed.ac.uk"
        Truth.assertThat(email_pattern.matcher(email).matches()).isTrue()
    }
}