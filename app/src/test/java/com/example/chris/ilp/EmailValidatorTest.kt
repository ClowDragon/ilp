package com.example.chris.ilp

import com.google.common.truth.Truth
import org.junit.Test
import java.util.regex.Pattern


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
        val email = "chris@123.com"
        Truth.assertThat(email_pattern.matcher(email).matches()).isTrue()
    }
}