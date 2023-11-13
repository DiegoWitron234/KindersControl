package com.miraimx.kinderscontrol.cuenta

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
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

class SingUpUsuario : AppCompatActivity(), Propiedades {
    private var esUsuarioActual: Boolean = false

    private lateinit var binding: ActivitySingUpEmpleadoBinding
    private var rutaStorage: String = ""
    private var rutaRTDB: String = ""
    private lateinit var rol: String
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

        //val emailEmpleado = intent?.getStringExtra("correo")

        rol = intent.getStringExtra("rol").toString()

        rutaStorage = when (rol) {
            "Admin" -> {
                binding.tituloRegistro.text = "Registrar nuevo administrador"
                binding.tituloTomarFotografia.text = "Fotografía del administrador"
                "administradores"
            }

            "Tutor" -> {
                binding.tituloRegistro.text = "Registrar nuevo tutor"
                binding.tituloTomarFotografia.text = "Fotografía del tutor"
                "tutores"
            }

            else -> {
                binding.tituloRegistro.text = "Registrar nuevo profesor"
                binding.tituloTomarFotografia.text = "Fotografía del profesor"
                "profesores"
            }
        }

        edCorreo.setText(currentUser?.email)

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

        binding.btnFotografia.setOnClickListener {
            if (currentUser != null) {
                val intent = Intent(this, Camara::class.java)
                activityResultLauncher.launch(intent)
            }
        }

        // Listener para el botón "Registrar"
        binding.btnRegEmpleado.setOnClickListener {
            if (currentUser != null) {
                registrar(currentUser.uid, currentUser.email, rol)
            } else {
                Toast.makeText(this, "No es posible registrar el usuario", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Agregar el callback para el botón "Back"
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val mensaje: String
                if (esUsuarioActual) {
                    mensaje = "¿Seguro que quieres cerrar la sesión?"
                    val flags = FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK
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
        AlertDialog.Builder(this@SingUpUsuario)
            .setMessage(mensaje)
            .setPositiveButton("Salir") { _, _ -> // Acción de confirmación
                if (flag != 0) {
                    Toast.makeText(this@SingUpUsuario, "Sesión cerrada", Toast.LENGTH_SHORT)
                        .show()
                    Firebase.auth.signOut()
                    val intent = Intent(this@SingUpUsuario, Login::class.java)
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

    private fun registrar(idUsuario: String?, emailUsuario: String?, rol: String) {
        val edNombre = binding.edEmpleadoNombre.text.toString()
        val edApellidos = binding.edEmpleadoApellidos.text.toString()
        val edTelefono = binding.edEmpleadoTelefono.text.toString()
        if (validarDatos(edNombre, edApellidos, emailUsuario, edTelefono)) {
            subirFoto { url ->
                guardarEnDatabase(
                    idUsuario,
                    edNombre,
                    edApellidos,
                    edTelefono,
                    emailUsuario,
                    rol,
                    url
                )
            }
        }
    }

    private fun subirFoto(onSuccess: (String) -> Unit) {
        if (fotoAlmacenada != null) {
            val storage = Firebase.storage
            val reference = storage.reference
            val imageRef = reference.child("$rutaStorage/${currentUser!!.uid}.png")
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
        idUsuario: String?,
        edNombre: String,
        edApellidos: String,
        edTelefono: String,
        emailUsuario: String?,
        rol: String,
        fotoEnlace: String
    ) {
        val database = FirebaseDatabase.getInstance()
        val empleadoRef = database.getReference("usuarios")

        // Crea la información del empleado
        val empleadoInfo =
            crearInfo(idUsuario, edNombre, edApellidos, edTelefono, emailUsuario, rol, fotoEnlace)

        // Guarda la información del empleado en la base de datos
        guardarInfo(empleadoRef.child(idUsuario!!), empleadoInfo)
    }

    private fun crearInfo(
        id: String?,
        edNombre: String,
        edApellidos: String,
        edTelefono: String,
        emailUsuario: String?,
        rol: String,
        fotoEnlace: String
    ): HashMap<String, Any?> {
        return hashMapOf(
            "usuario_id" to id,
            "nombre_usuario" to edNombre,
            "apellidos_usuario" to edApellidos,
            "telefono_usuario" to edTelefono,
            "correo_usuario" to emailUsuario,
            "foto_usuario" to fotoEnlace,
            "rol" to rol,
            "estado" to true
        )
    }

    private fun guardarInfo(usuarioRef: DatabaseReference, usuarioInfo: HashMap<String, Any?>) {
        usuarioRef.setValue(usuarioInfo).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(
                    this,
                    when (rol) {
                        "Admin" -> PanelAdmin::class.java
                        "Tutor" -> MainPanelTutor::class.java
                        else -> MainPanelProfesor::class.java
                    }
                ).apply { flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK }
                startActivity(intent)
                Toast.makeText(this@SingUpUsuario, "Usuario registrado", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@SingUpUsuario, "Ha ocurrido un error", Toast.LENGTH_SHORT)
                    .show()
            }
        }.addOnCanceledListener{
            Toast.makeText(this@SingUpUsuario, "No se registró el usuario", Toast.LENGTH_LONG)
                .show()
        }
    }
}