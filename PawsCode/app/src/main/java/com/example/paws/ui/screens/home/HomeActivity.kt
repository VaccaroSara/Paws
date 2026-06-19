package com.example.paws.ui.screens.home

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.paws.R

class HomeActivity : AppCompatActivity() {

    private val homeFragment = HomeFragment()
    private val addPuppyFragment = AddPuppyFragment()
    private val favoritesFragment = FavoritesFragment()
    private val profileFragment = ProfileFragment()
    
    private var activeFragment: Fragment = homeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val navHome = findViewById<FrameLayout>(R.id.nav_home)
        val navPlus = findViewById<FrameLayout>(R.id.nav_plus)
        val navHeart = findViewById<FrameLayout>(R.id.nav_heart)
        val navProfile = findViewById<FrameLayout>(R.id.nav_profile)

        val navViews = listOf(navHome, navPlus, navHeart, navProfile)

        // Initialize fragments
        supportFragmentManager.beginTransaction().apply {
            add(R.id.content_frame, profileFragment, "4").hide(profileFragment)
            add(R.id.content_frame, favoritesFragment, "3").hide(favoritesFragment)
            add(R.id.content_frame, addPuppyFragment, "2").hide(addPuppyFragment)
            add(R.id.content_frame, homeFragment, "1")
        }.commit()

        navHome.isSelected = true

        navViews.forEach { view ->
            view.setOnClickListener {
                if (view.isSelected) return@setOnClickListener

                navViews.forEach { it.isSelected = false }
                view.isSelected = true
                
                val nextFragment: Fragment = when (view.id) {
                    R.id.nav_home -> homeFragment
                    R.id.nav_plus -> addPuppyFragment
                    R.id.nav_heart -> {
                        Log.d("HomeActivity", "Switching to FavoritesFragment")
                        favoritesFragment
                    }
                    R.id.nav_profile -> profileFragment
                    else -> homeFragment
                }
                
                showFragment(nextFragment)
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        // Quando cambiamo tab dalla navbar, dobbiamo "pulire" la navigazione interna (backstack)
        // per evitare che rimangano aperti profili o dettagli sopra le tab principali.
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(fragment)
            .commit()
        activeFragment = fragment
    }
}
