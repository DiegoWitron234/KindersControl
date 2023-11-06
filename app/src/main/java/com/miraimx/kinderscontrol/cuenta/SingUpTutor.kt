package com.miraimx.kinderscontrol.cuenta

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.storageMetadata
import com.miraimx.kinderscontrol.Propiedades
import com.miraimx.kinderscontrol.R
import com.miraimx.kinderscontrol.databinding.ActivitySingUpTutorBinding
import java.io.File

class SingUpTutor : AppCompatActivity(), Propiedades {

    private lateinit var binding: ActivitySingUpTutorBinding
    private var fotoAlmacenada: String? = null
    val currentUser = FirebaseAuth.getInstance().currentUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingUpTutorBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
                }
            }
        }

        val btnRegEmpleado = binding.btnRegTutor
        val edCorreo = binding.edTutorCorreo

        binding.btnTutorFotografia.setOnClickListener {
            if (currentUser != null) {
                val intent =
                    Intent(this, Camara::class.java)
                activityResultLauncher.launch(intent)
            }
        }

        val emailEmpleado = intent?.getStringExtra("correo")

        edCorreo.setText(emailEmpleado)

        btnRegEmpleado.setOnClickListener {
            registrar(emailEmpleado, currentUser)
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                alerta()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
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
        apellidos: String,
        edad: String,
        telefono: String,
        correo: String?,
        direccion: String,
    ): Boolean {
        val regex = Regex("^[0-9]{10}$")

        if (nombre.isEmpty() || edad.isEmpty() || telefono.isEmpty() || direccion.isEmpty() ||
            correo!!.isEmpty() || apellidos.isEmpty()
        ) {
            Toast.makeText(this, "Verifique que los campos no esten vacios", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        if (!regex.matches(telefono)) {
            Toast.makeText(this, "Teléfono no valido", Toast.LENGTH_SHORT).show()
            return false
        }


        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Dirección de correo no valido", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun registrar(emailEmpleado: String?, currentUser: FirebaseUser?) {
        val edNombre = findViewById<EditText>(R.id.edTutorNombre).text.toString()
        val edApellidos = binding.edTutorApellidos.text.toString()
        val edEdad = findViewById<EditText>(R.id.edTutorEdad).text.toString()
        val edTelefono = binding.edTutorTelefono.text.toString()
        val edDireccion = findViewById<EditText>(R.id.edTutorDireccion).text.toString()

        if (validarDatos(
                edNombre,
                edApellidos,
                edEdad,
                edTelefono,
                emailEmpleado,
                edDireccion
            ) && currentUser != null
        ) {
            subirFoto { url ->
                guardarEnDatabase(
                    currentUser.uid,
                    edNombre,
                    edApellidos,
                    edEdad,
                    edTelefono,
                    edDireccion,
                    emailEmpleado!!,
                    url
                )
            }
        }
    }

    private fun subirFoto(onSuccess: (String) -> Unit) {
        val storage = Firebase.storage
        val reference = storage.reference
        val imageRef = reference.child("tutores/${currentUser!!.uid}.png")
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
    }

    private fun guardarEnDatabase(
        idUsuario: String,
        edNombre: String,
        edApellidos: String,
        edEdad: String,
        edTelefono: String,
        edDireccion: String,
        emailEmpleado: String,
        fotoEnlace: String
    ) {
        val database = FirebaseDatabase.getInstance()
        val alumnoRef = database.getReference("tutores").child(idUsuario)

        // Determina si el empleado es un usuario actual o un nuevo usuario

        // Crea la información del empleado
        val alumnoInfo = crearInfo(
            idUsuario,
            edNombre,
            edApellidos,
            edEdad,
            edTelefono,
            edDireccion,
            emailEmpleado,
            fotoEnlace
        )

        // Guarda la información del empleado en la base de datos
        guardarInfo(alumnoRef, alumnoInfo)

    }

    private fun crearInfo(
        idUsuario: String,
        edNombre: String,
        edApellidos: String,
        edEdad: String,
        edTelefono: String,
        edDireccion: String,
        emailEmpleado: String,
        fotoEnlace: String
    ): HashMap<String, Any?> {
        return hashMapOf(
            "tutor_id" to idUsuario,
            "nombre_tutor" to edNombre,
            "apellidos_tutor" to edApellidos,
            "edad_tutor" to edEdad,
            "telefono_tutor" to edTelefono,
            "direccion_tutor" to edDireccion,
            "correo_tutor" to emailEmpleado,
            "foto_tutor" to fotoEnlace
        )
    }

    private fun guardarInfo(empleadoRef: DatabaseReference, empleadoInfo: HashMap<String, Any?>) {
        empleadoRef.setValue(empleadoInfo).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Alumno registrado", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
            }
        }
    }

}