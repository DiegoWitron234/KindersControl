package com.miraimx.kinderscontrol

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class PanelUsuario : AppCompatActivity() {

    private lateinit var btnCerrarSesion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_usuario)

        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        btnCerrarSesion.setOnClickListener { cerrarSesion() }

        val btnMostrarQR = findViewById<Button>(R.id.btnMostrarQR)
        btnMostrarQR.setOnClickListener{}
    }


    private fun cerrarSesion() {
        AlertDialog.Builder(this)
            .setMessage("¿Seguro que quieres cerrar la sesión?")
            .setPositiveButton("Salir") { _, _ -> // Acción de confirmación
                Firebase.auth.signOut()
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun verificarUsuario() {
        val database = FirebaseDatabase.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val uRef = database.getReference("tutores").child(uid)
            uRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        val intent = Intent(this@PanelUsuario, SingUpTutor::class.java)
                        intent.putExtra("correo",currentUser.email)
                        startActivity(intent)
                        Toast.makeText(this@PanelUsuario, "Registrando Tutor", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this@PanelUsuario, "Existe", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PanelUsuario, "onCancelled", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    override fun onStart() {
        super.onStart()
        verificarUsuario()
    }
}