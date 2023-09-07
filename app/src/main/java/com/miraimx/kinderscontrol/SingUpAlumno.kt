package com.miraimx.kinderscontrol

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SingUpAlumno : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_up_alumno)
        val btnRegEmpleado = findViewById<Button>(R.id.btnRegAlumno)

        btnRegEmpleado.setOnClickListener {
            registrar()
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                alerta()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun registrar() {
        val edMatricula = findViewById<EditText>(R.id.matricula)
        val edNombre = findViewById<EditText>(R.id.edAluNombre)
        val edEdad = findViewById<EditText>(R.id.edAluEdad)
        val edTSangre = findViewById<EditText>(R.id.edAluSangre)
        val edGradoGrupo = findViewById<EditText>(R.id.edAluGradoGrupo)

        val database = FirebaseDatabase.getInstance()
        val usuarioRef = database.getReference("alumnos")

        val usuarioInfo = hashMapOf(
            "matricula" to edMatricula,
            "nombre_alumno" to edNombre.text.toString(),
            "edad_alumno" to edEdad.text.toString(),
            "tiposangre_alumno" to edTSangre.text.toString(),
            "grado_grupo" to edGradoGrupo.text.toString(),
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

    private fun alerta(){
        val mensaje = "¿Desea salir del registro?"

        AlertDialog.Builder(this@SingUpAlumno)
            .setMessage(mensaje)
            .setPositiveButton("Salir") { _, _ -> // Acción de confirmación
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}