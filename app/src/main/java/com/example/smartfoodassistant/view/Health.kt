package com.example.smartfoodassistant.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smartfoodassistant.R

class Health : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_health)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI elements
        val productNameInput = findViewById<EditText>(R.id.productNameInput)
        val manualSubmitButton = findViewById<Button>(R.id.manualSubmitButton)
        val productInfo = findViewById<TextView>(R.id.productInfo)
        val healthInfo = findViewById<TextView>(R.id.healthInfo)
        val qrCode = findViewById<ImageView>(R.id.qrCode)
        val aiRecommendation = findViewById<TextView>(R.id.aiRecommendation)

        // Set click listener for the submit button
        manualSubmitButton.setOnClickListener {
            val productName = productNameInput.text.toString()
            // Call the method to analyze the product
            analyzeProduct(productName)
        }
    }

    private fun analyzeProduct(product: String) {
        // Recommendations based on the product name
        val recommendation = when (product.lowercase()) {
            "milk" -> "Keep in the fridge."
            "bread" -> "Store in a cool, dry place."
            "vegetables" -> "Keep in the fridge."
            "fruits" -> "Store at room temperature or in the fridge."
            "meat" -> "Must be refrigerated to avoid spoilage."
            "eggs" -> "Store in the fridge for maximum freshness."
            "cheese" -> "Keep in the fridge."
            "yogurt" -> "Store in the fridge."
            // Add more products here...
            else -> "No specific recommendation available."
        }

        // Health benefits based on the product name
        val healthBenefits = when (product.lowercase()) {
            "milk" -> "Rich in calcium and vitamin D, beneficial for bones."
            "bread" -> "Good source of carbohydrates, especially whole grain varieties."
            "vegetables" -> "High in vitamins and minerals, essential for a balanced diet."
            "fruits" -> "Provide vitamins, fiber, and antioxidants for health."
            "meat" -> "High in protein, essential for muscle growth."
            "eggs" -> "Excellent source of protein and essential nutrients."
            "cheese" -> "Contains calcium and beneficial fats in moderation."
            "yogurt" -> "Probiotic benefits for gut health."
            // Add more health benefits for additional products here...
            else -> "Health benefits information not available."
        }

        // Update the UI with recommendations and health benefits
        findViewById<TextView>(R.id.productInfo).text = recommendation
        findViewById<TextView>(R.id.healthInfo).text = healthBenefits
    }
}
