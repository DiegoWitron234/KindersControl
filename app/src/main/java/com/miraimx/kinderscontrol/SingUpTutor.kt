package com.miraimx.kinderscontrol

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class SingUpTutor : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_up_tutor)
        val btnRegEmpleado = findViewById<Button>(R.id.btnRegTutor)

        btnRegEmpleado.setOnClickListener {
            registrar()
        }
    }

    private fun registrar() {
        val edNombre = findViewById<EditText>(R.id.edTutorNombre)
        val edEdad = findViewById<EditText>(R.id.edTutorEdad)
        val edTelefono = findViewById<EditText>(R.id.edTutorTelefono)
        val edCorreo = findViewById<EditText>(R.id.edTutorCorreo)
        val edDireccion = findViewById<EditText>(R.id.edTutorDireccion)

        val database = FirebaseDatabase.getInstance()
        val usuarioRef = database.getReference("tutores")
        val nuevoUsuario = usuarioRef.push()
        val idUsuario = nuevoUsuario.key

        val usuarioInfo = hashMapOf(
            "tutor_id" to idUsuario,
            "nombre_tutor" to edNombre.text.toString(),
            "edad_tutor" to edEdad.text.toString(),
            "telefono_tutor" to edTelefono.text.toString(),
            "correo_tutor" to edCorreo.text.toString(),
            "direccion_tutor" to edDireccion.text.toString()
        )

        usuarioRef.setValue(usuarioInfo)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Tutor registrado", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
                }
            }
    }
}