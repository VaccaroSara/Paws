package com.example.paws

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val navHome = findViewById<FrameLayout>(R.id.nav_home)
        val navPlus = findViewById<FrameLayout>(R.id.nav_plus)
        val navHeart = findViewById<FrameLayout>(R.id.nav_heart)
        val navProfile = findViewById<FrameLayout>(R.id.nav_profile)

        val navItems = listOf(navHome, navPlus, navHeart, navProfile)

        // Seleziona la home di default
        navHome.isSelected = true

        navItems.forEach { item ->
            item.setOnClickListener {
                // Deseleziona tutti
                navItems.forEach { it.isSelected = false }
                // Seleziona quello cliccato
                it.isSelected = true
            }
        }
    }
}