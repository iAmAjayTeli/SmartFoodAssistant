package com.example.smartfoodassistant.view

import android.content.res.AssetFileDescriptor
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.smartfoodassistant.R
import com.example.smartfoodassistant.databinding.ActivityMainBinding
import com.example.smartfoodassistant.model.DataCrud
import com.example.smartfoodassistant.model.FoodDetails
import com.example.smartfoodassistant.model.WeatherResponse
import com.example.smartfoodassistant.retrofit.WeatherService
import com.example.smartfoodassistant.util.Constants.WEATHER_API
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseFirestore
    private val weatherService = WeatherService()
    private lateinit var tflite: Interpreter

    // Define the number of input features for your model
    private companion object {
        const val NUM_INPUT_FEATURES = 3 // Adjust this based on your model's requirements
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = FirebaseFirestore.getInstance()

        // Initialize the TensorFlow Lite model
        if (!loadModel()) {
            Log.e("TFLite", "Failed to initialize TensorFlow Lite model.")
            // Optionally show a message to the user or disable prediction
        }

        // Load the TensorFlow Lite model
        loadModel()

        // Set up button click listener
        clickListener()

        // Fetch weather data
        val city = "Bengaluru"
        val apiKey = WEATHER_API
        fetchWeatherData(city, apiKey)
    }

    private fun loadModel(): Boolean {
        return try {
            tflite = Interpreter(loadModelFile("model.tflite")) // Load the model from assets
            true // Return true if loading was successful
        } catch (e: Exception) {
            Log.e("Model", "Error loading model: ${e.message}")
            false // Return false if there was an error
        }
    }

    private fun loadModelFile(modelPath: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset: Long = fileDescriptor.startOffset
        val declaredLength: Long = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }


    private fun clickListener() {
        binding.apply {
            manualSubmitButton.setOnClickListener {
                // Check if the tflite interpreter has been initialized
                if (!::tflite.isInitialized) {
                    Log.e("TFLite", "TensorFlow Lite interpreter is not initialized.")
                    aiRecommendation.text = "Model is not available."
                    return@setOnClickListener
                }



                val foodType = productNameInput.text.toString()
                val temp = temperatureInput.text.toString().toDouble()
                val humidity = humidityInput.text.toString().toDouble()
                val expiry = expiryDate.text.toString()

                if (productNameInput.text.isEmpty() || temperatureInput.text.isEmpty() || humidityInput.text.isEmpty() || expiryDate.text.isEmpty()) {
                    Toast.makeText(this@MainActivity, "Please fill all fields correctly.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }


                val food = FoodDetails(foodType, temp, humidity, expiry)

                lifecycleScope.launch(Dispatchers.IO) {
                    DataCrud.storeData(food)
                }

                // Make a prediction and get the recommendation
                val recommendation = makePrediction(foodType, temp, humidity, expiry)
                aiRecommendation.text = recommendation
            }
        }
    }

//    private fun makePrediction(
//        product: String,
//        temperature: Double,
//        humidity: Double,
//        expiry: String
//    ): String {
//        // Calculate age from expiry date
//        val age = calculateAge(expiry)
//
//        // Prepare input data
//        val inputData = preprocessData(product, temperature, humidity, age)
//        val outputData = Array(1) { FloatArray(3) } // Adjust size based on your model output
//
//        // Run inference
//        tflite.run(arrayOf(inputData), outputData) // Pass the input as an array
//
//        if (outputData[0].isNotEmpty()) { // Check if the first row is not empty
//            // Process output and return the recommendation
//            return processOutput(outputData)
//        } else {
//            // Handle empty output
//            Log.w("MainActivity", "TensorFlow Lite model produced empty output.")
//            return "Model prediction failed. Please check input data." // Or any other appropriate message
//        }
//    }


    //for temporary usage,, later we will trained model
    private fun makePrediction(product: String, temperature: Double, humidity: Double, expiry: String): String {
        return when (product.lowercase()) {
            "milk" -> {
                if (temperature < 4) "Keep in the fridge." else "Store in the fridge to prevent spoilage."
            }
            "bread" -> {
                if (humidity > 60) "Store in a cool, dry place." else "Can be stored at room temperature."
            }
            "vegetables" -> {
                if (temperature < 10) "Keep in the fridge." else "Consume soon, store in a cooler place."
            }
            "fruits" -> {
                when {
                    humidity < 60 -> "Store in a cool, dry place."
                    temperature < 20 -> "Keep at room temperature."
                    else -> "Store in the fridge to extend freshness."
                }
            }
            "meat" -> {
                if (temperature < 4) "Keep in the fridge." else "Must be refrigerated to avoid spoilage."
            }
            "eggs" -> "Store in the fridge for maximum freshness."
            "cheese" -> "Keep in the fridge and consume by the expiry date."
            "yogurt" -> "Store in the fridge and check for expiry."
            "fish" -> {
                if (temperature < 0) "Keep in the freezer." else "Store in the fridge and consume within 1-2 days."
            }
            "pasta" -> {
                if (humidity < 50) "Store in a cool, dry place." else "Keep in an airtight container."
            }
            "rice" -> "Store in a cool, dry place in an airtight container."
            "cereal" -> "Keep in a cool, dry place, sealed tightly."
            "frozen vegetables" -> "Store in the freezer until ready to use."
            "jam" -> "Keep in the fridge after opening."
            "nuts" -> {
                if (humidity < 60) "Store in a cool, dry place." else "Refrigerate to prevent rancidity."
            }
            "chips" -> "Store in a cool, dry place, sealed tightly."
            "soda" -> "Keep in a cool place. Refrigerate for best taste."
            "chocolate" -> "Store in a cool, dry place; refrigerate if hot."
            "honey" -> "Keep in a cool, dry place. No need to refrigerate."
            "spices" -> "Store in a cool, dry place, sealed tightly."
            else -> "No specific recommendation available. Check product guidelines."
        }
    }





    private fun preprocessData(
        product: String,
        temperature: Double,
        humidity: Double,
        age: Int
    ): FloatArray {
        val inputData = FloatArray(NUM_INPUT_FEATURES)

        // Fill inputData with the necessary values
        inputData[0] = temperature.toFloat()
        inputData[1] = humidity.toFloat()
        inputData[2] = age.toFloat()
        // You may need to one-hot encode the product name or use an appropriate encoding method

        return inputData
    }


//    private fun calculateAge(expiryDate: String): Int {
//        return try {
//            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            val expiry = sdf.parse(expiryDate) ?: Date()
//            val currentDate = Date()
//            val diff = expiry.time - currentDate.time
//            (diff / (1000 * 60 * 60 * 24)).toInt() // Convert milliseconds to days
//        } catch (e: Exception) {
//            Log.e("DateError", "Invalid date format")
//            0 // Default to 0 if there's an error
//        }
//    }

//    private fun processOutput(outputData: Array<FloatArray>): String {
//        // Process output to get the recommendation
//        val maxIndex = outputData[0].indices.maxByOrNull { outputData[0][it] } ?: 0
//        val classes = arrayOf("Low", "Medium", "High") // Replace with your actual classes
//        return "Risk Level: ${classes[maxIndex]}"
//    }

    private fun fetchWeatherData(city: String, apiKey: String) {
        weatherService.api.getWeatherData(city, apiKey).enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    weatherResponse?.let {
                        val temperature = it.main.temperature
                        val humidity = it.main.humidity

                        // Use the temperature and humidity in your app
                        Log.d("Weather", "Temperature: $temperature°C, Humidity: $humidity%")

                        binding.apply {
                            temperatureInput.setText(temperature.toString())
                            humidityInput.setText(humidity.toString())
                        }
                    }
                } else {
                    Log.e("Weather", "API response failed")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.e("Weather", "Network request failed: ${t.message}")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::tflite.isInitialized) {
            tflite.close()
        }
    }
}
