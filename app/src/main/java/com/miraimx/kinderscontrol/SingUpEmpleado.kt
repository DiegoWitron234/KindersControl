package com.miraimx.kinderscontrol

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SingUpEmpleado : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_up_empleado)

        val btnRegEmpleado = findViewById<Button>(R.id.btnRegEmpleado)

        btnRegEmpleado.setOnClickListener {
            registrar()
        }

        // Agregar el callback para el botón "Back"
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@SingUpEmpleado)
                    .setMessage("¿Seguro que quieres cerrar la sesión?")
                    .setPositiveButton("Salir") { _, _ -> // Acción de confirmación
                        Firebase.auth.signOut()
                        Toast.makeText(this@SingUpEmpleado, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SingUpEmpleado, Login::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun registrar() {
        val database = FirebaseDatabase.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val edNombre = findViewById<EditText>(R.id.edEmpleadoNombre)
        val edPuesto = findViewById<EditText>(R.id.edEmpleadoPuesto)
        val edCorreo = findViewById<EditText>(R.id.edEmpleadoCorreo)

        if (currentUser != null) {
            val uid = currentUser.uid
            val empleadoRef = database.getReference("empleados").child(uid)

            val empleadoInfo =  hashMapOf(
                "empleado_id" to uid,
                "nombre" to edNombre.text.toString(),
                "puesto" to edPuesto.text.toString(),
                "correo_empleado" to edCorreo.text.toString()
            )

            empleadoRef.setValue(empleadoInfo)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Empleado registrado", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

}