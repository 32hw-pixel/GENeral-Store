package com.example

import com.example.data.PasswordStrength
import com.example.data.SecurityUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

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

  @Test
  fun `test password strength evaluation`() {
    assertEquals(PasswordStrength.WEAK, SecurityUtils.evaluatePasswordStrength("123"))
    assertEquals(PasswordStrength.MEDIUM, SecurityUtils.evaluatePasswordStrength("Secret1"))
    assertEquals(PasswordStrength.STRONG, SecurityUtils.evaluatePasswordStrength("SuperSecret123!"))
  }

  @Test
  fun `test reset code generation`() {
    val code = SecurityUtils.generateResetCode()
    assertEquals(6, code.length)
    assertTrue(code.all { it.isDigit() })
  }
}

