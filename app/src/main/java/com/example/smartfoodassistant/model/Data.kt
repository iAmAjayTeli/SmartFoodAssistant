package com.example.smartfoodassistant.model

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


data class FoodDetails(
    val foodType: String,
    val temperature: Double,
    val humidity: Double,
    val expiryDate: String
)


class DataCrud {
    companion object {
        private val userRef = Firebase.firestore.collection("FoodSafetyData")

        suspend fun storeData(foodDetails: FoodDetails) {
            userRef.add(foodDetails)
        }
    }


}