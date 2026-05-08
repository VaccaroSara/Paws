package com.example.paws

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 1. Troviamo la scritta "SIGN UP" (Il bersaglio)
        val tabSignUp = findViewById<TextView>(R.id.tabSignUp_Xa)

        // 2. Mettiamo l'orecchio in ascolto del click
        tabSignUp.setOnClickListener {

            // 3. Prepariamo il taxi (Intent) verso la pagina Xb
            val taxi = Intent(this, SignUpActivity::class.java)

            // 4. Partenza!
            startActivity(taxi)
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}