package com.example.travelsphere

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Use XML layout

        try {
            val button: Button = findViewById(R.id.my_button)
            button.setOnClickListener {
                // Handle button click
            }
        } catch (e: Exception) {
            e.printStackTrace() // This will log any error to Logcat
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }

    }
}
