package com.example.paws.ui.screens.home

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.paws.R

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val navHome = findViewById<FrameLayout>(R.id.nav_home)
        val navPlus = findViewById<FrameLayout>(R.id.nav_plus)
        val navHeart = findViewById<FrameLayout>(R.id.nav_heart)
        val navProfile = findViewById<FrameLayout>(R.id.nav_profile)

        val navViews = listOf(navHome, navPlus, navHeart, navProfile)

        // Seleziona la home di default all'avvio
        navHome.isSelected = true
        replaceFragment(HomeFragment())

        navViews.forEach { view ->
            view.setOnClickListener {
                // Deseleziona tutte le icone
                navViews.forEach { it.isSelected = false }
                // Seleziona quella cliccata
                view.isSelected = true
                
                // Determina quale fragment mostrare in base all'ID cliccato
                val fragment: Fragment = when (view.id) {
                    R.id.nav_home -> HomeFragment()
                    R.id.nav_plus -> AddPuppyFragment()
                    R.id.nav_heart -> {
                        Log.d("HomeActivity", "Switching to FavoritesFragment")
                        FavoritesFragment()
                    }
                    R.id.nav_profile -> ProfileFragment()
                    else -> HomeFragment()
                }
                
                replaceFragment(fragment)
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .commit()
    }
}