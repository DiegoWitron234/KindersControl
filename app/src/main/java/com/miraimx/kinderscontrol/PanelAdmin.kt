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

class PanelAdmin : AppCompatActivity() {

    // Intervalo de tiempo para considerar dos pulsaciones seguidas
    private lateinit var btnAgregarEmpleado: Button
    private lateinit var btnCerrarSesion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_admin)

        btnAgregarEmpleado = findViewById(R.id.btnAgregarEmpleado)
        val btnAgregarAlumno = findViewById<Button>(R.id.btnAgregarNino)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        btnAgregarEmpleado.setOnClickListener {
            val intent = Intent(this, RegistrarUsuario::class.java)
            intent.putExtra("rol", "Admin")
            startActivity(intent)
        }

        btnAgregarAlumno.setOnClickListener(){
            val intent = Intent(this, SingUpAlumno::class.java)
            startActivity(intent)
        }

        btnCerrarSesion.setOnClickListener { cerrarSesion() }
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

    override fun onStart() {
        super.onStart()
        verificarUsuario()
    }

    private fun verificarUsuario() {
        val database = FirebaseDatabase.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val uRef = database.getReference("empleados").child(uid)
            uRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        val intent = Intent(this@PanelAdmin, SingUpEmpleado::class.java)
                        intent.putExtra("id", uid)
                        intent.putExtra("correo",currentUser.email)
                        startActivity(intent)
                        Toast.makeText(this@PanelAdmin, "Registrando Empleado", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this@PanelAdmin, "Existe", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PanelAdmin, "onCancelled", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}