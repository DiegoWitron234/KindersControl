package com.miraimx.kinderscontrol

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class SingUpAlumno : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_up_alumno)
        val btnRegEmpleado = findViewById<Button>(R.id.btnRegAlumno)

        btnRegEmpleado.setOnClickListener {
            registrar()
        }
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
}