package com.example.paws

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.util.Pair
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        // 1. Troviamo la scritta "SIGN UP" (Il bersaglio)
        val tabSignUp = findViewById<TextView>(R.id.tabSignUp_Xa)

        // 2. Mettiamo l'orecchio in ascolto del click
        tabSignUp.setOnClickListener {

            // Prepariamo gli elementi condivisi per la transizione
            val logo = findViewById<View>(R.id.logo_Xa)
            val tabSignIn = findViewById<View>(R.id.tabSignIn_Xa)
            val lineSignIn = findViewById<View>(R.id.lineSignIn_Xa)
            val lineSignUp = findViewById<View>(R.id.lineSignUp_Xa)
            val username = findViewById<View>(R.id.username_Xa)
            val password = findViewById<View>(R.id.password_Xa)
            val btnLogin = findViewById<View>(R.id.btnLogin_Xa)

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                Pair.create(logo, "logo_transition"),
                Pair.create(tabSignIn, "tab_signin_transition"),
                Pair.create(tabSignUp, "tab_signup_transition"),
                Pair.create(lineSignIn, "line_signin_transition"),
                Pair.create(lineSignUp, "line_signup_transition"),
                Pair.create(username, "username_transition"),
                Pair.create(password, "password_transition"),
                Pair.create(btnLogin, "button_transition")
            )

            // 3. Prepariamo il taxi (Intent) verso la pagina Xb
            val taxi = Intent(this, SignUpActivity::class.java)

            // 4. Partenza con transizione!
            // startActivity(taxi, options.toBundle())
            
            // Per ora andiamo direttamente alla HomeActivity per vedere il risultato
            val homeIntent = Intent(this, HomeActivity::class.java)
            startActivity(homeIntent)

        }

        // Troviamo anche il bottone SIGN IN per andare alla Home
        val btnLogin = findViewById<android.view.View>(R.id.btnLogin_Xa)
        btnLogin.setOnClickListener {
            val homeIntent = Intent(this, HomeActivity::class.java)
            startActivity(homeIntent)
        }

    }
}