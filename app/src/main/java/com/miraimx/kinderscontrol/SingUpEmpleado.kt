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
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.miraimx.kinderscontrol.databinding.ActivitySingUpEmpleadoBinding

class SingUpEmpleado : AppCompatActivity(), ModoOscuro {
    private var esUsuarioActual: Boolean = false
    private lateinit var binding: ActivitySingUpEmpleadoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingUpEmpleadoBinding.inflate(layoutInflater)

        setContentView(binding.root)
        cancelarModoOscuro(this)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        val btnRegEmpleado = findViewById<Button>(R.id.btnRegEmpleado)
        val edCorreo = findViewById<EditText>(R.id.edEmpleadoCorreo)

        val emailEmpleado = intent?.getStringExtra("correo")
        val idEmpleado = intent?.getStringExtra("id")

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
        val edNombre = findViewById<EditText>(R.id.edEmpleadoNombre).text.toString()
        val edPuesto = findViewById<EditText>(R.id.edEmpleadoPuesto).text.toString()

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
            if (id != null){
                empleadoRef = empleadoRef.child(id)
            }
            Toast.makeText(this, "Es usuario Nuevo", Toast.LENGTH_LONG).show()
        }

        if (validarDatos(edNombre, edPuesto, emailEmpleado)){
            val empleadoInfo = hashMapOf(
                "empleado_id" to id,
                "nombre_empleado" to edNombre,
                "puesto_empleado" to edPuesto,
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

    private fun validarDatos(nombre: String, puesto: String, email: String?): Boolean {
        if (nombre.isEmpty() || puesto.isEmpty() || email!!.isEmpty()) {
            Toast.makeText(this, "Verifique que los campos no esten vacios", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Dirección de correo no valido", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}