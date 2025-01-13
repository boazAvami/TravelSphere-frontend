// MainActivity.kt
package com.syb.travelsphere

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.syb.travelsphere.pages.AddPostFragment
import com.syb.travelsphere.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.syb.travelsphere.pages.AllPostsFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load the AllPostsFragment by default
        if (savedInstanceState == null) {
            loadFragment(AddPostFragment())
        }

    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
