package com.example.paws.ui.screens.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import com.example.paws.R
import com.example.paws.ui.screens.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // UI references
        val etEmail = findViewById<EditText>(R.id.username_Xa) // Using username field as email
        val etPassword = findViewById<EditText>(R.id.password_Xa)
        val btnLogin = findViewById<Button>(R.id.btnLogin_Xa)
        val tabSignUp = findViewById<TextView>(R.id.tabSignUp_Xa)

        // Change hint to Email to avoid confusion
        etEmail.hint = "Email"

        btnLogin.setOnClickListener {
            signInUser(etEmail.text.toString(), etPassword.text.toString())
        }

        tabSignUp.setOnClickListener {
            val logo = findViewById<View>(R.id.logo_Xa)
            val tabSignIn = findViewById<View>(R.id.tabSignIn_Xa)
            val lineSignIn = findViewById<View>(R.id.lineSignIn_Xa)
            val lineSignUp = findViewById<View>(R.id.lineSignUp_Xa)
            val username = findViewById<View>(R.id.username_Xa)
            val password = findViewById<View>(R.id.password_Xa)
            val btnLoginShared = findViewById<View>(R.id.btnLogin_Xa)

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                Pair.create(logo, "logo_transition"),
                Pair.create(tabSignIn, "tab_signin_transition"),
                Pair.create(tabSignUp, "tab_signup_transition"),
                Pair.create(lineSignIn, "line_signin_transition"),
                Pair.create(lineSignUp, "line_signup_transition"),
                Pair.create(username, "username_transition"),
                Pair.create(password, "password_transition"),
                Pair.create(btnLoginShared, "button_transition")
            )

            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent, options.toBundle())
        }
    }

    private fun signInUser(email: String, pass: String) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Inserisci l'email", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email non valida", Toast.LENGTH_SHORT).show()
            return
        }
        if (pass.isEmpty()) {
            Toast.makeText(this, "Inserisci la password", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Accesso eseguito!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Errore: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}