package com.example.paws

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Assicurati che qui ci sia il nome del file XML corretto per la registrazione
        setContentView(R.layout.activity_sign_up)

        // 1. Troviamo la scritta "SIGN IN" in questa pagina
        // NOTA BENE: Controlla che questo ID (tabSignIn_Xb) sia esattamente quello
        // che hai scritto nel file activity_sign_up.xml! Se lì l'hai chiamato diversamente, cambialo qui.
        val tabSignIn = findViewById<TextView>(R.id.tabSignIn_Xb)

        // 2. Mettiamo l'orecchio in ascolto del click
        tabSignIn.setOnClickListener {

            // 3. Invece di un Intent, chiudiamo questa pagina!
            // Il telefono tornerà in automatico alla MainActivity che era rimasta sotto.
            finish()
        }
    }
}