package com.example.paws

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Assicurati che qui ci sia il nome del file XML corretto per la registrazione
        setContentView(R.layout.activity_sign_up)

        // Configurazione dello Spinner per il tipo di account
        val spinnerAccountType = findViewById<Spinner>(R.id.spinnerAccountType_Xb)
        val containerAccountType = findViewById<android.view.View>(R.id.containerAccountType_Xb)

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.account_types,
            R.layout.spinner_item
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerAccountType.adapter = adapter

        // Facciamo in modo che cliccando su tutto il contenitore (incluso il triangolino) si apra il menu
        containerAccountType.setOnClickListener {
            spinnerAccountType.performClick()
        }

        // 1. Troviamo la scritta "SIGN IN" in questa pagina
        // NOTA BENE: Controlla che questo ID (tabSignIn_Xb) sia esattamente quello
        // che hai scritto nel file activity_sign_up.xml! Se lì l'hai chiamato diversamente, cambialo qui.
        val tabSignIn = findViewById<TextView>(R.id.tabSignIn_Xb)

        // 2. Mettiamo l'orecchio in ascolto del click
        tabSignIn.setOnClickListener {

            // 3. Invece di un Intent, chiudiamo questa pagina con la transizione!
            // Il telefono tornerà in automatico alla MainActivity che era rimasta sotto.
            supportFinishAfterTransition()
        }
    }
}