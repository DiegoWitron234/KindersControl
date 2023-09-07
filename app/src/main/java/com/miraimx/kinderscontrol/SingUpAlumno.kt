package com.miraimx.kinderscontrol

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                alerta()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun registrar() {
        val edMatricula = findViewById<EditText>(R.id.matricula).text.toString()
        val edNombre = findViewById<EditText>(R.id.edAluNombre).text.toString()
        val edEdad = findViewById<EditText>(R.id.edAluEdad).text.toString()
        val edTSangre = findViewById<EditText>(R.id.edAluSangre).text.toString()
        val edGradoGrupo = findViewById<EditText>(R.id.edAluGradoGrupo).text.toString()

        val database = FirebaseDatabase.getInstance()
        val usuarioRef = database.getReference("alumnos")

        if (validarDatosAlumno(edMatricula, edNombre, edEdad, edTSangre, edGradoGrupo)){
            val usuarioInfo = hashMapOf(
                "matricula" to edMatricula,
                "nombre_alumno" to edNombre,
                "edad_alumno" to edEdad,
                "tiposangre_alumno" to edTSangre,
                "grado_grupo" to edGradoGrupo,
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

    private fun validarDatosAlumno(
        matricula: String,
        nombre: String,
        edad: String,
        tipoSangre: String,
        gradoGrupo: String
    ): Boolean {
        if (matricula.isEmpty() || nombre.isEmpty() || edad.isEmpty() ||
            tipoSangre.isEmpty() || gradoGrupo.isEmpty()) {
            Toast.makeText(this, "Verifique que los campos no esten vacios", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}