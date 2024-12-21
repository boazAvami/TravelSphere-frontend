package com.syb.travelsphere


import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val emailInput = findViewById<EditText>(R.id.email_input)
        val saveButton = findViewById<Button>(R.id.save_button)

        saveButton.setOnClickListener {
            val email = emailInput.text.toString()
            Toast.makeText(this, "Profile updated with email: $email", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}