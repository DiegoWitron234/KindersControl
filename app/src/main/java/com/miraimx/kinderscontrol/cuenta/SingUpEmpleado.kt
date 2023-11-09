package com.miraimx.kinderscontrol.cuenta

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.storageMetadata
import com.miraimx.kinderscontrol.Propiedades
import com.miraimx.kinderscontrol.administrador.PanelAdmin
import com.miraimx.kinderscontrol.databinding.ActivitySingUpEmpleadoBinding
import com.miraimx.kinderscontrol.profesor.MainPanelProfesor
import com.miraimx.kinderscontrol.tutor.MainPanelTutor
import java.io.File

class SingUpEmpleado : AppCompatActivity(), Propiedades {
    private var esUsuarioActual: Boolean = false
    private lateinit var binding: ActivitySingUpEmpleadoBinding
    private var ruta: String = ""
    private lateinit var puesto: String
    private var fotoAlmacenada: String? = null
    val currentUser = FirebaseAuth.getInstance().currentUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingUpEmpleadoBinding.inflate(layoutInflater)

        setContentView(binding.root)
        cancelarModoOscuro(this)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)


        val edCorreo = binding.edEmpleadoCorreo

        val emailEmpleado = intent?.getStringExtra("correo")

        puesto = intent.getStringExtra("rol").toString()

        ruta = if (puesto == "Admin") {
            binding.tituloRegistro.text = "Registrar nuevo administrador"
            binding.tituloTomarFotografia.text = "Fotografía del administrador"
            "administradores"
        } else {
            binding.tituloRegistro.text = "Registrar nuevo profesor"
            binding.tituloTomarFotografia.text = "Fotografía del profesor"
            "profesores"
        }


        edCorreo.setText(emailEmpleado)

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

        binding.btnFotografia.setOnClickListener {
            if (currentUser != null) {
                val intent = Intent(this, Camara::class.java)
                activityResultLauncher.launch(intent)
            }
        }

        // Listener para el botón "Registrar"
        binding.btnRegEmpleado.setOnClickListener {
            if (currentUser != null && fotoAlmacenada != null) {
                registrar(currentUser.uid, emailEmpleado, puesto!!)
            }else{
                Toast.makeText(this, "NO es posible registrar", Toast.LENGTH_SHORT).show()
            }
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

    private fun alerta(mensaje: String, flag: Int) {
        AlertDialog.Builder(this@SingUpEmpleado)
            .setMessage(mensaje)
            .setPositiveButton("Salir") { _, _ -> // Acción de confirmación
                if (flag != 0) {
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

    private fun validarDatos(
        nombre: String,
        apellidos: String,
        email: String?,
        telefono: String
    ): Boolean {
        val regex = Regex("^[0-9]{10}$")

        if (nombre.isEmpty() || apellidos.isEmpty() || email!!.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Verifique que los campos no esten vacios", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Dirección de correo no valido", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!regex.matches(telefono)) {
            Toast.makeText(this, "Teléfono no valido", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun registrar(idEmpleado: String?, emailEmpleado: String?, puesto: String) {
        val edNombre = binding.edEmpleadoNombre.text.toString()
        val edApellidos = binding.edEmpleadoApellidos.text.toString()
        val edTelefono = binding.edEmpleadoTelefono.text.toString()
        if (validarDatos(edNombre, edApellidos, emailEmpleado, edTelefono)) {
            subirFoto { url ->
                guardarEnDatabase(
                    idEmpleado,
                    edNombre,
                    edApellidos,
                    edTelefono,
                    emailEmpleado,
                    puesto,
                    url
                )
            }
        }
        val intent = Intent(
            this,
            when (puesto) {
                "Admin" -> PanelAdmin::class.java
                else -> MainPanelProfesor::class.java
            }
        )
        startActivity(intent)
        finish()
    }

    private fun subirFoto(onSuccess: (String) -> Unit) {
        val storage = Firebase.storage
        val reference = storage.reference
        val imageRef = reference.child("$ruta/${currentUser!!.uid}.png")
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
        idEmpleado: String?,
        edNombre: String,
        edApellidos: String,
        edTelefono: String,
        emailEmpleado: String?,
        puesto: String,
        fotoEnlace: String
    ) {
        val database = FirebaseDatabase.getInstance()
        val empleadoRef = database.getReference("empleados")

        // Determina si el empleado es un usuario actual o un nuevo usuario
        val id = determinarTipoDeUsuario(idEmpleado, empleadoRef)

        // Crea la información del empleado
        val empleadoInfo = crearInfo(id, edNombre, edApellidos, edTelefono, emailEmpleado, puesto, fotoEnlace)

        // Guarda la información del empleado en la base de datos
        guardarInfo(empleadoRef.child(id!!), empleadoInfo)
    }

    private fun determinarTipoDeUsuario(idEmpleado: String?, empleadoRef: DatabaseReference): String? {
        return if (idEmpleado != null) {
            esUsuarioActual = true
            empleadoRef.child(idEmpleado)
            idEmpleado
        } else {
            esUsuarioActual = false
            val nuevoEmpleado = empleadoRef.push()
            val id = nuevoEmpleado.key
            if (id != null) {
                empleadoRef.child(id)
            }
            Toast.makeText(this@SingUpEmpleado, "Es usuario Nuevo", Toast.LENGTH_LONG).show()
            id
        }
    }

    private fun crearInfo(
        id: String?,
        edNombre: String,
        edApellidos: String,
        edTelefono: String,
        emailEmpleado: String?,
        puesto: String,
        fotoEnlace: String
    ): HashMap<String, Any?> {
        return hashMapOf(
            "empleado_id" to id,
            "nombre_empleado" to edNombre,
            "apellidos_empleado" to edApellidos,
            "telefono_empleado" to edTelefono,
            "correo_empleado" to emailEmpleado,
            "foto_empleado" to fotoEnlace,
            "puesto" to puesto
        )
    }

    private fun guardarInfo(empleadoRef: DatabaseReference, empleadoInfo: HashMap<String, Any?>) {
        empleadoRef.setValue(empleadoInfo).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@SingUpEmpleado, "Empleado registrado", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@SingUpEmpleado, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}