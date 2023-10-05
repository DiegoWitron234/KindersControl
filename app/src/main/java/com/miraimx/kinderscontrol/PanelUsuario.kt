package com.miraimx.kinderscontrol

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase


class PanelUsuario : AppCompatActivity() {

    private lateinit var btnCerrarSesion: Button
    private lateinit var uid: String
    private lateinit var btnMostrarQR: Button
    private lateinit var currentUser: FirebaseUser
    private lateinit var serviceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_usuario)

        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        btnMostrarQR = findViewById(R.id.btnMostrarQR)

        btnCerrarSesion.setOnClickListener { cerrarSesion() }

        currentUser = FirebaseAuth.getInstance().currentUser!!

        mostrarAsignaciones()

        btnMostrarQR.setOnClickListener {
            fnMostrarQR()
        }

        serviceIntent = Intent(this, ServicioOyente::class.java)
        startService(serviceIntent)
        // Agrega un oyente a la referencia de "checkin" en la base de datos
        val checkinRef = FirebaseDatabase.getInstance().getReference("checkin")

        checkinRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Itera a través de todos los hijos de "checkin"
                for (childSnapshot in dataSnapshot.children) {
                    // Obtiene el valor del atributo "tutor_id" de cada registro
                    val tutorId = childSnapshot.child("tutor_id").getValue(String::class.java)

                    // Comprueba si "tutor_id" coincide con tu variable global "uid"
                    if (tutorId == uid) {
                        // Si coincide, muestra un Toast y registra los datos en Logcat
                        val checkinId = childSnapshot.child("check_id").getValue(String::class.java)
                        val horafecha = childSnapshot.child("horafecha_check").getValue(String::class.java)
                        val inOut = childSnapshot.child("in_out").getValue(String::class.java)
                        val matricula = childSnapshot.child("matricula").getValue(String::class.java)

                        // Muestra un Toast
                        //Toast.makeText(applicationContext, "Se agregó un registro con ID: $checkinId", Toast.LENGTH_SHORT).show()

                        // Registra los datos en Logcat
                        Log.d("Registro", "Registro agregado:")
                        Log.d("Registro", "check_id: $checkinId")
                        Log.d("Registro", "horafecha_check: $horafecha")
                        Log.d("Registro", "in_out: $inOut")
                        Log.d("Registro", "matricula: $matricula")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Maneja errores de lectura de la base de datos
                Log.w("Registro", "Error al leer la base de datos: $error", error.toException())
            }
        })
    }

    override fun onStart() {
        super.onStart()
        verificarUsuario()
    }

    private fun mostrarAsignaciones() {
        val cUserId = currentUser.uid
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
                stopService(serviceIntent)
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun verificarUsuario() {
        val database = FirebaseDatabase.getInstance()
        uid = currentUser.uid
        val uRef = database.getReference("tutores").child(uid)
        uRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val intent = Intent(this@PanelUsuario, SingUpTutor::class.java)
                    intent.putExtra("correo", currentUser.email)
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PanelUsuario, "onCancelled", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fnMostrarQR() {
        val intent = Intent(this, DisplayQRActivity::class.java)
        intent.putExtra("uid", uid)
        startActivity(intent)
    }

}