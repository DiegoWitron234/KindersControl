package com.miraimx.kinderscontrol

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SingUpTutor : AppCompatActivity(), ModoOscuro {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_up_tutor)
        cancelarModoOscuro(this)
        val btnRegEmpleado = findViewById<Button>(R.id.btnRegTutor)
        val edCorreo = findViewById<EditText>(R.id.edTutorCorreo)

        val emailEmpleado = intent?.getStringExtra("correo")

        edCorreo.setText(emailEmpleado)

        btnRegEmpleado.setOnClickListener {
            registrar(emailEmpleado)
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                alerta()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun registrar(emailEmpleado: String?) {
        val edNombre = findViewById<EditText>(R.id.edTutorNombre).text.toString()
        val edEdad = findViewById<EditText>(R.id.edTutorEdad).text.toString()
        val edTelefono = findViewById<EditText>(R.id.edTutorTelefono).text.toString()
        val edDireccion = findViewById<EditText>(R.id.edTutorDireccion).text.toString()

        val database = FirebaseDatabase.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val idUsuario = currentUser.uid
            val usuarioRef = database.getReference("tutores").child(idUsuario)

            if (validarDatos(edNombre, edEdad, edTelefono, edDireccion, emailEmpleado)) {
                val usuarioInfo = hashMapOf(
                    "tutor_id" to idUsuario,
                    "nombre_tutor" to edNombre,
                    "edad_tutor" to edEdad,
                    "telefono_tutor" to edTelefono,
                    "direccion_tutor" to edDireccion,
                    "correo_tutor" to emailEmpleado
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
    }

    private fun alerta() {
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

    private fun validarDatos(
        nombre: String,
        edad: String,
        telefono: String,
        direccion: String,
        correo: String?
    ): Boolean {
        val regex = Regex("^[0-9]{10}$")

        if (nombre.isEmpty() || edad.isEmpty() || telefono.isEmpty() || direccion.isEmpty() ||
            correo!!.isEmpty()) {
            Toast.makeText(this, "Verifique que los campos no esten vacios", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!regex.matches(telefono)){
            Toast.makeText(this, "Teléfono no valido", Toast.LENGTH_SHORT).show()
            return false
        }


        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Dirección de correo no valido", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

}