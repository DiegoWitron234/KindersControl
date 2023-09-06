package com.miraimx.kinderscontrol

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class PanelUsuario : AppCompatActivity() {

    private lateinit var btnCerrarSesion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_usuario)

        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        btnCerrarSesion.setOnClickListener { cerrarSesion() }
    }

    private fun cerrarSesion(){
        AlertDialog.Builder(this)
            .setMessage("¿Seguro que quieres cerrar la sesión?")
            .setPositiveButton("Salir") { _, _ -> // Acción de confirmación
                Firebase.auth.signOut()
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}