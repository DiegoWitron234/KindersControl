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

class SingUpEmpleado : AppCompatActivity() {
    private var esUsuarioActual: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_up_empleado)

        val btnRegEmpleado = findViewById<Button>(R.id.btnRegEmpleado)
        val edCorreo = findViewById<EditText>(R.id.edEmpleadoCorreo)

        val emailEmpleado = intent?.getStringExtra("correo")
        val idEmpleado = intent?.getStringExtra("id")
        Toast.makeText(this, emailEmpleado, Toast.LENGTH_SHORT).show()
        edCorreo.setText(emailEmpleado)

        btnRegEmpleado.setOnClickListener {
            registrar(idEmpleado, emailEmpleado)
        }

        // Agregar el callback para el botón "Back"
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val mensaje: String
                if (esUsuarioActual) {
                    mensaje = "¿Seguro que quieres cerrar la sesión?"
                    val flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    alerta(mensaje, flags)
                } else {
                    mensaje = "¿Seguro que quiere salir del registro?"
                    alerta(mensaje, 0)
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun registrar(idEmpleado: String?, emailEmpleado: String?) {
        val edNombre = findViewById<EditText>(R.id.edEmpleadoNombre)
        val edPuesto = findViewById<EditText>(R.id.edEmpleadoPuesto)


        val database = FirebaseDatabase.getInstance()
        var empleadoRef = database.getReference("empleados")
        val id: String?

        if (idEmpleado != null) {
            esUsuarioActual = true
            empleadoRef = empleadoRef.child(idEmpleado)
            id = idEmpleado
        } else {
            esUsuarioActual = false
            val nuevoEmpleado = empleadoRef.push()
            id = nuevoEmpleado.key
        }

        val empleadoInfo = hashMapOf(
            "empleado_id" to id,
            "nombre_empleado" to edNombre.text.toString(),
            "puesto_empleado" to edPuesto.text.toString(),
            "correo_empleado" to emailEmpleado
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

    private fun alerta(mensaje: String, flag: Int){
        AlertDialog.Builder(this@SingUpEmpleado)
            .setMessage(mensaje)
            .setPositiveButton("Salir") { _, _ -> // Acción de confirmación
                if (flag != 0){
                    Toast.makeText(this@SingUpEmpleado, "Sesión cerrada", Toast.LENGTH_SHORT)
                        .show()
                    Firebase.auth.signOut()
                    val intent = Intent(this@SingUpEmpleado, Login::class.java)
                    intent.addFlags(flag)
                    startActivity(intent)
                }
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}