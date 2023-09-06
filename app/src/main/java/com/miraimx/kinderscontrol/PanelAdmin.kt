package com.miraimx.kinderscontrol

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class PanelAdmin : AppCompatActivity() {

    private var backPressedTime: Long = 0
    private val backPressedInterval: Long = 2000
    // Intervalo de tiempo para considerar dos pulsaciones seguidas
    private lateinit var btnAgregarEmpleado: Button
    private lateinit var btnCerrarSesion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_admin)

        btnAgregarEmpleado = findViewById(R.id.btnAgregarEmpleado)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        btnAgregarEmpleado.setOnClickListener {
            val intent = Intent(this, RegistrarUsuario::class.java)
            intent.putExtra("rol", "Admin")
            startActivity(intent)
        }

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