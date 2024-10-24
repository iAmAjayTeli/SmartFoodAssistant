package com.example.smartfoodassistant.workmanager

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CheckExpiry {
    companion object {

        fun checkExpiry(expiryDate: String): Boolean {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expiry = sdf.parse(expiryDate)
            val today = Calendar.getInstance().time

            return (expiry?.time?.minus(today.time) ?: 0) < 2 * 24 * 60 * 60 * 1000 // 2 days
        }
        
    }
}