package com.miraimx.kinderscontrol

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private var backPressedTime: Long = 0
    private val backPressedInterval: Long = 2000
    // Intervalo de tiempo para considerar dos pulsaciones seguidas
    private lateinit var textView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)


        // Agregar el callback para el botón "Back"
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Obtener el tiempo actual
                val currentTime = System.currentTimeMillis()

                if (currentTime - backPressedTime < backPressedInterval) {
                    // Si se pulsa dos veces dentro del intervalo, cerrar la aplicación
                    finishAffinity()
                } else {
                    // Si no, mostrar mensaje para volver a pulsar
                    backPressedTime = currentTime
                    Toast.makeText(this@MainActivity, "Presiona de nuevo para salir", Toast.LENGTH_SHORT).show()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    fun fnCerrarSesion(view: View) {
        Firebase.auth.signOut()
        finish()
    }
}