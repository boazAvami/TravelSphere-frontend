package com.syb.travelsphere

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.syb.travelsphere.pages.AllPostsActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnOpenMap: Button = findViewById(R.id.btnOpenMap)
        btnOpenMap.setOnClickListener {
            startActivity(Intent(this, AllPostsActivity::class.java))
        }
    }
}
