package com.syb.travelsphere.utils

import android.util.Patterns
import com.google.android.material.textfield.TextInputLayout

object InputValidator {
    private const val PASSWORD_MIN_LENGTH: Int = 6

    // Validate Email
    fun validateEmail(email: String, inputLayout: TextInputLayout?): Boolean {
        return when {
            email.isEmpty() -> {
                inputLayout?.error = "⚠️ Please enter your email"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                inputLayout?.error = "⚠️ Please enter a valid email"
                false
            }
            else -> {
                inputLayout?.error = null
                true
            }
        }
    }

    // Validate Password
    fun validatePassword(password: String, inputLayout: TextInputLayout?): Boolean {
        return when {
            password.isEmpty() -> {
                inputLayout?.error = "⚠️ Please enter your password"
                false
            }
            password.length < PASSWORD_MIN_LENGTH -> { // Adjust the length requirement if needed
                inputLayout?.error = "⚠️ Password must be at least 6 characters"
                false
            }
            else -> {
                inputLayout?.error = null
                true
            }
        }
    }

    // Validate Username
    fun validateUsername(username: String, inputLayout: TextInputLayout?): Boolean {
        return when {
            username.isEmpty() -> {
                inputLayout?.error = "⚠️ Username is required"
                false
            }
            else -> {
                inputLayout?.error = null
                true
            }
        }
    }

    // Validate Phone Number
    fun validatePhoneNumber(phone: String, inputLayout: TextInputLayout?): Boolean {
        return when {
            phone.isEmpty() -> {
                inputLayout?.error = "⚠️ Phone number is required"
                false
            }
            !phone.matches(Regex("^[0-9]{9,15}$")) -> {
                inputLayout?.error = "⚠️ Invalid phone number format"
                false
            }
            else -> {
                inputLayout?.error = null
                true
            }
        }
    }

    // Validate Description
    fun validateDescription(description: String, inputLayout: TextInputLayout?): Boolean {
        return when {
            description.isEmpty() -> {
                inputLayout?.error = "⚠️ Description is required"
                false
            }
            else -> {
                inputLayout?.error = null
                true
            }
        }
    }

    // Validate Required Filed
    fun validateRequiredTextField(inputText: String, inputLayout: TextInputLayout?): Boolean {
        return when {
            inputText.isEmpty() -> {
                inputLayout?.error = "⚠️ This filed is required"
                false
            }
            else -> {
                inputLayout?.error = null
                true
            }
        }
    }
}
