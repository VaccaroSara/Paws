package com.example.paws.ui.screens.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.paws.R
import com.example.paws.ui.screens.home.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Hide keyboard when clicking background
        findViewById<View>(android.R.id.content).setOnClickListener { hideKeyboard() }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etFirstName = findViewById<EditText>(R.id.firstName_Xb)
        val etLastName = findViewById<EditText>(R.id.lastName_Xb)
        val etCity = findViewById<EditText>(R.id.city_Xb)
        val etProvince = findViewById<EditText>(R.id.province_Xb)
        val etCap = findViewById<EditText>(R.id.cap_Xb)
        val etUsername = findViewById<EditText>(R.id.username_Xb)
        val etEmail = findViewById<EditText>(R.id.email_Xb)
        val etPassword = findViewById<EditText>(R.id.password_Xb)
        val etPhone = findViewById<EditText>(R.id.phone_Xb)
        val spinnerAccountType = findViewById<Spinner>(R.id.spinnerAccountType_Xb)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp_Xb)

        btnSignUp.setOnClickListener {
            val firstName = etFirstName.text.toString()
            val lastName = etLastName.text.toString()
            val city = etCity.text.toString()
            val province = etProvince.text.toString()
            val cap = etCap.text.toString()
            val username = etUsername.text.toString()
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()
            val phone = etPhone.text.toString()
            val accountType = spinnerAccountType.selectedItem.toString()
            
            signUpUser(firstName, lastName, city, province, cap, username, email.trim().lowercase(), pass, phone, accountType)
        }

        val containerAccountType = findViewById<View>(R.id.containerAccountType_Xb)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.account_types,
            R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerAccountType.adapter = adapter

        containerAccountType.setOnClickListener {
            spinnerAccountType.performClick()
        }

        val tabSignIn = findViewById<TextView>(R.id.tabSignIn_Xb)
        tabSignIn.setOnClickListener {
            supportFinishAfterTransition()
        }
    }

    private fun signUpUser(firstName: String, lastName: String, city: String, province: String, cap: String, username: String, email: String, pass: String, phone: String, accountType: String) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Inserisci un'email", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email non valida", Toast.LENGTH_SHORT).show()
            return
        }
        if (pass.isEmpty() || pass.length < 6) {
            Toast.makeText(this, "La password deve essere di almeno 6 caratteri", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        // Check if username is unique
                        db.collection("users")
                            .whereEqualTo("username", username)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                if (querySnapshot.isEmpty) {
                                    saveUserData(uid, firstName, lastName, city, province, cap, username, email, phone, accountType)
                                } else {
                                    // Username already exists, delete the auth user just created
                                    auth.currentUser?.delete()
                                    Toast.makeText(this, "Username già in uso. Scegline un altro.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .addOnFailureListener {
                                auth.currentUser?.delete()
                                Toast.makeText(this, "Errore verifica username: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Errore: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserData(uid: String?, firstName: String, lastName: String, city: String, province: String, cap: String, username: String, email: String, phone: String, accountType: String) {
        if (uid == null) return

        val user = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "city" to city,
            "province" to province,
            "cap" to cap,
            "username" to username,
            "email" to email,
            "phone" to phone,
            "accountType" to accountType
        )

        db.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Registrazione completata!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Errore salvataggio dati: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: View(this)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}