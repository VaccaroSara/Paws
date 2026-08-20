package com.example.paws

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUnitTest {

    // Helper functions representing credential validation logic in Auth screens
    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    private fun doPasswordsMatch(pass: String, confirmPass: String): Boolean {
        return pass == confirmPass
    }

    private fun isFormComplete(vararg fields: String): Boolean {
        return fields.all { it.isNotBlank() }
    }

    @Test
    fun passwordLength_mustBeAtLeastSixCharacters() {
        assertTrue(isPasswordValid("123456"))
        assertTrue(isPasswordValid("securePassword123"))
        assertFalse(isPasswordValid("12345"))
        assertFalse(isPasswordValid(""))
    }

    @Test
    fun passwordConfirmation_matchesCorrectly() {
        assertTrue(doPasswordsMatch("pass123", "pass123"))
        assertFalse(doPasswordsMatch("pass123", "pass456"))
    }

    @Test
    fun formCompletion_verifiesAllFieldsNonEmpty() {
        assertTrue(isFormComplete("Mario", "Rossi", "mario@example.com", "3801234567"))
        assertFalse(isFormComplete("Mario", "", "mario@example.com", "3801234567"))
    }
}
