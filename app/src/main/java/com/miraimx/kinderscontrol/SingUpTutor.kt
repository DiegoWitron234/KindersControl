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

class SingUpTutor : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_up_tutor)
        val btnRegEmpleado = findViewById<Button>(R.id.btnRegTutor)

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
        val edNombre = findViewById<EditText>(R.id.edTutorNombre)
        val edEdad = findViewById<EditText>(R.id.edTutorEdad)
        val edTelefono = findViewById<EditText>(R.id.edTutorTelefono)
        val edCorreo = findViewById<EditText>(R.id.edTutorCorreo)
        val edDireccion = findViewById<EditText>(R.id.edTutorDireccion)

        val database = FirebaseDatabase.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if  (currentUser != null){
            val idUsuario = currentUser.uid
            val usuarioRef = database.getReference("tutores").child(idUsuario)

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

    private fun alerta(){
        val mensaje = "¿Seguro que quieres cerrar la sesión?"
        val flag = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        AlertDialog.Builder(this@SingUpTutor)
            .setMessage(mensaje)
            .setPositiveButton("Salir") { _, _ -> // Acción de confirmación
                Toast.makeText(this@SingUpTutor, "Sesión cerrada", Toast.LENGTH_SHORT)
                    .show()
                Firebase.auth.signOut()
                val intent = Intent(this@SingUpTutor, Login::class.java)
                intent.addFlags(flag)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}