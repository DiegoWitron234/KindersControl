package com.miraimx.kinderscontrol

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


class PanelUsuario : AppCompatActivity() {

    private lateinit var btnCerrarSesion: Button
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_usuario)

        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        btnCerrarSesion.setOnClickListener { cerrarSesion() }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("Mensaje", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = "Token: $token"
            Log.d("Mensaje", msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })

        val btnMostrarQR = findViewById<Button>(R.id.btnMostrarQR)
        btnMostrarQR.setOnClickListener{}
        mostrarAsignaciones()
    }

    override fun onStart() {
        super.onStart()
        verificarUsuario()
    }

    private fun mostrarAsignaciones() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val cUserId = currentUser?.uid.toString()
        findViewById<Button>(R.id.btnMostrarNinos).setOnClickListener {
            startActivity(
                Intent(
                    this@PanelUsuario,
                    AsignacionesTutor::class.java
                ).putExtra("currentId", cUserId)
            )
        }
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
            uid = currentUser.uid
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

    fun fnMostrarQR(view: View) {
        val intent = Intent(this, DisplayQRActivity::class.java)
        intent.putExtra("uid", uid)
        startActivity(intent)
    }
}