package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SecurityUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("GENeral Chat", appName)
  }

  @Test
  fun `test password hashing and verification`() {
    val password = "SuperSecret123!"
    val salt = SecurityUtils.generateSalt()
    val hash = SecurityUtils.hashPassword(password, salt)

    assertTrue(SecurityUtils.verifyPassword(password, salt, hash))
    assertFalse(SecurityUtils.verifyPassword("WrongPassword", salt, hash))
  }

  @Test
  fun `test email validation`() {
    assertTrue(SecurityUtils.isValidEmail("user@example.com"))
    assertTrue(SecurityUtils.isValidEmail("test.name+tag@sub.domain.org"))
    assertFalse(SecurityUtils.isValidEmail("invalid-email"))
    assertFalse(SecurityUtils.isValidEmail("@example.com"))
  }
}
