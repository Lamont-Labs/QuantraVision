package com.lamontlabs.quantravision.utils

object ValidationUtils {
    
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        return email.matches(emailRegex)
    }
    
    fun isValidPrice(price: String): Boolean {
        val priceRegex = "^\\d+(\\.\\d{1,2})?\$".toRegex()
        return price.matches(priceRegex) && price.toDoubleOrNull()?.let { it > 0 } == true
    }
    
    fun isValidPercentage(percentage: String): Boolean {
        return percentage.toDoubleOrNull()?.let { it in 0.0..100.0 } == true
    }
    
    fun isValidConfidence(confidence: Float): Boolean {
        return confidence in 0f..1f
    }
    
    fun sanitizeInput(input: String): String {
        return input.trim().replace(Regex("[<>\"']"), "")
    }
}
