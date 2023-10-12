package com.miraimx.kinderscontrol

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.FirebaseDatabase
import com.miraimx.kinderscontrol.databinding.ActivitySingUpAlumnoBinding

class SingUpAlumno : AppCompatActivity(), ModoOscuro {
    private lateinit var svEdad: Spinner
    private lateinit var svSangre: Spinner
    private lateinit var svGrado: Spinner
    private lateinit var svGrupo: Spinner
    private lateinit var binding: ActivitySingUpAlumnoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySingUpAlumnoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cancelarModoOscuro(this)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        scrollDatos()
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
        val edEdad = svEdad.selectedItem.toString()
        val edTSangre = svSangre.selectedItem.toString()
        val edGradoGrupo = svGrado.selectedItem.toString() + svGrupo.selectedItem.toString()


        val database = FirebaseDatabase.getInstance()
        val usuarioRef = database.getReference("alumnos").child(edMatricula)

        if (validarDatosAlumno(edMatricula, edNombre, edEdad, edTSangre, edGradoGrupo)) {
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

    private fun alerta() {
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
            tipoSangre.isEmpty() || gradoGrupo.isEmpty()
        ) {
            Toast.makeText(this, "Verifique que los campos no esten vacios", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }


    private fun scrollDatos() {
        svEdad = findViewById(R.id.svAlumnoEdad)
        svSangre = findViewById(R.id.svAlumnoSangre)
        svGrado = findViewById(R.id.svAlumnoGrado)
        svGrupo = findViewById(R.id.svAlumnoGrupo)
        val datosEdad = listOf("3", "4","5","6","7")
        val datosSangre = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val grupo = listOf("A", "B", "C")
        val grado = listOf("1", "2", "3")

        adapterScroll(svEdad, datosEdad)
        adapterScroll(svSangre, datosSangre)
        adapterScroll(svGrado, grado)
        adapterScroll(svGrupo, grupo)

    }

    private fun adapterScroll(vista: Spinner, lista: List<String>) {
        val arrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, lista)
        vista.adapter = arrayAdapter
    }
}