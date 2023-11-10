package com.miraimx.kinderscontrol.cuenta

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.storageMetadata
import com.miraimx.kinderscontrol.Propiedades
import com.miraimx.kinderscontrol.R
import com.miraimx.kinderscontrol.databinding.ActivitySingUpAlumnoBinding
import java.io.File

class SingUpAlumno : AppCompatActivity(), Propiedades {
    private lateinit var svEdad: Spinner
    private lateinit var svSangre: Spinner
    private lateinit var svGrado: Spinner
    private lateinit var svGrupo: Spinner
    private lateinit var binding: ActivitySingUpAlumnoBinding
    private var matricula: String? = null
    private var ruta: String? = null
    private var fotoAlmacenada: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySingUpAlumnoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnFotografia.isEnabled = false
        cancelarModoOscuro(this)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        // Recibe el enlace de la activity Camera
        val activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val datos = data?.getStringExtra("imagen")
                if (!datos.isNullOrEmpty()) {
                    fotoAlmacenada = datos
                    binding.mensajeFotografia.apply {
                        text = Html.fromHtml("<b>Fotografía guardada</b>", 0x12)
                        textSize = 15f
                        setTextColor(Color.BLACK)
                    }
                }
            }
        }

        scrollDatos()
        val btnRegEmpleado = binding.btnRegAlumno

        btnRegEmpleado.setOnClickListener {
            registrar()
        }

        binding.matricula.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    binding.btnFotografia.isEnabled = true
                    matricula = binding.matricula.text.toString()
                    ruta = "alumnos/$matricula"
                } else {
                    binding.btnFotografia.isEnabled = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })
        binding.btnFotografia.setOnClickListener {
            val intent = Intent(this@SingUpAlumno, Camara::class.java)
            activityResultLauncher.launch(intent)
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                alerta()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
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

    private fun validarDatos(
        nombre: String,
        apellidos: String,
        edad: String,
        tipoSangre: String,
    ): Boolean {
        if (matricula.isNullOrEmpty() || nombre.isEmpty() || edad.isEmpty() ||
            tipoSangre.isEmpty() || apellidos.isEmpty()
        ) {
            Toast.makeText(this, "Verifique que los campos no esten vacios", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }

    private fun registrar() {
        val edNombre = binding.edAluNombre.text.toString()
        val edEdad = svEdad.selectedItem.toString()
        val edTSangre = svSangre.selectedItem.toString()
        val edApellidos = binding.edAluApellido.text.toString()
        val edGrupo = svGrupo.selectedItem.toString()
        val edGrado = svGrado.selectedItem.toString()
        if (validarDatos(edNombre, edApellidos, edEdad, edTSangre)) {
            subirFoto { url ->
                guardarEnDatabase(
                    edNombre,
                    edApellidos,
                    edEdad,
                    edTSangre,
                    edGrado,
                    edGrupo,
                    url
                )
            }
        }
    }

    private fun subirFoto(onSuccess: (String) -> Unit) {
        if (fotoAlmacenada != null) {
            val storage = Firebase.storage
            val reference = storage.reference
            val imageRef = reference.child("$ruta.png")
            val metadata = storageMetadata {
                contentType = imageRef.toString()
            }
            val file = fotoAlmacenada?.let { File(it) }
            val fileUri = Uri.fromFile(file)
            imageRef.putFile(fileUri, metadata).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val fotoEnlace = uri.toString()
                    onSuccess(fotoEnlace)

                    if (file != null && file.exists()) {
                        file.delete()
                    }
                }
            }.addOnFailureListener {
                Log.e("Log", "Imagen no subida")
            }
        } else {
            Toast.makeText(this, "Por favor tome una foto", Toast.LENGTH_SHORT).show()
            binding.mensajeFotografia.apply {
                text = "No se ha guardado alguna foto"
                setTextColor(Color.RED)
            }
        }
    }

    private fun guardarEnDatabase(
        edNombre: String,
        edApellidos: String,
        edEdad: String,
        edTSangre: String,
        edGrado: String,
        edGrupo: String,
        fotoEnlace: String
    ) {
        val database = FirebaseDatabase.getInstance()
        val alumnoRef = database.getReference("alumnos").child(matricula!!)

        // Crea la información del empleado
        val alumnoInfo =
            crearInfo(edNombre, edApellidos, edEdad, edTSangre, edGrado, edGrupo, fotoEnlace)

        // Guarda la información del empleado en la base de datos
        guardarInfo(alumnoRef, alumnoInfo)

    }

    private fun crearInfo(
        edNombre: String,
        edApellidos: String,
        edEdad: String,
        edTSangre: String,
        edGrado: String,
        edGrupo: String,
        fotoEnlace: String
    ): HashMap<String, Any?> {
        return hashMapOf(
            "matricula" to matricula,
            "nombre_alumno" to edNombre,
            "apellidos_alumno" to edApellidos,
            "edad_alumno" to edEdad,
            "tiposangre_alumno" to edTSangre,
            "grado" to edGrado,
            "grupo" to edGrupo,
            "foto_alumno" to fotoEnlace,
            "estado" to true
        )
    }

    private fun guardarInfo(usuarioRef: DatabaseReference, usuarioInfo: HashMap<String, Any?>) {
        usuarioRef.setValue(usuarioInfo).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@SingUpAlumno, "Alumno registrado", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@SingUpAlumno, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scrollDatos() {
        svEdad = findViewById(R.id.svAlumnoEdad)
        svSangre = findViewById(R.id.svAlumnoSangre)
        svGrado = binding.svGrado
        svGrupo = binding.svGrupo
        val datosEdad = listOf("3", "4", "5", "6", "7")
        val datosSangre = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val datosGrupo = listOf("A", "B", "C")
        val datosGrado = listOf("1", "2", "3")
        adapterScroll(svEdad, datosEdad)
        adapterScroll(svSangre, datosSangre)
        adapterScroll(svGrado, datosGrado)
        adapterScroll(svGrupo, datosGrupo)
    }


    private fun adapterScroll(vista: Spinner, lista: List<String>) {
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lista)
        vista.adapter = arrayAdapter
    }
}