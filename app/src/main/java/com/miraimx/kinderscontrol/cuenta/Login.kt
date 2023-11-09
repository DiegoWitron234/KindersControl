package com.miraimx.kinderscontrol.cuenta

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.miraimx.kinderscontrol.Propiedades
import com.miraimx.kinderscontrol.administrador.PanelAdmin
import com.miraimx.kinderscontrol.databinding.ActivityLoginBinding
import com.miraimx.kinderscontrol.profesor.MainPanelProfesor
import com.miraimx.kinderscontrol.tutor.MainPanelTutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class Login : AppCompatActivity(), Propiedades {

    private lateinit var auth: FirebaseAuth

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cancelarModoOscuro(this)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        auth = Firebase.auth
        binding.btnLogin.isEnabled = false

        // Agregar el TextWatcher al campo de correo electrónico
        binding.editCorreo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val correoValido = s.isValidEmail()
                if (correoValido) {
                    binding.txtCorreoValido.text = ""
                } else {
                    binding.txtCorreoValido.text = "Ingrese un correo válido"
                }
                binding.btnLogin.isEnabled =
                    correoValido && binding.editContrasena.text.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Listener al campo de contraseña para habilitar o deshabilitar el botón de inicio de sesión
        binding.editContrasena.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnLogin.isEnabled =
                    binding.editCorreo.text.isValidEmail() && s?.isNotEmpty() == true
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Listener al botón de inicio de sesión (btnLogin) para realizar la acción de inicio de sesión
        binding.btnLogin.setOnClickListener {
            // Lógica del Login
            val correo: String = binding.editCorreo.text.toString()
            val password: String = binding.editContrasena.text.toString()
            //println("Correo: $correo \nContraseña: $password")
            signIn(correo, password)
        }

        // Listener para registrar un nuevo usuario
        binding.textRegistrar.setOnClickListener {
            // Intent para ir a la activity de registro de usuario
            val intent = Intent(this, RegistrarUsuario::class.java)
            intent.putExtra("rol", "Tutor")
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        verificacionRol()
    }

    // Función para validar el formato del correo electrónico
    private fun CharSequence?.isValidEmail(): Boolean {
        if (isNullOrEmpty()) return false
        return Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    private fun signIn(email: String, password: String) {
        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                    verificacionRol()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "No se pudo iniciar sesión.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    //Hacer algo si sale mal
                }
            }
        // [END sign_in_with_email]
    }

    private fun verificacionRol() {
        // Obtener una referencia a la base de datos
        val database = FirebaseDatabase.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val scope = CoroutineScope(Dispatchers.IO)

        // Verificar si el usuario está autenticado
        if (currentUser != null) {
            val uid = currentUser.uid
            val userRef = database.getReference("usuarios").child(uid)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Aquí puedes tomar decisiones basadas en el rol del usuario
                        when (val role = dataSnapshot.child("rol").value.toString()) {
                            "Admin" -> {
                                // Mostrar la interfaz de administrador
                                verificarUsuario(role)
                                //intent.putExtra("rol", role)
                            }

                            "Tutor" -> {
                                // Mostrar la interfaz de usuario
                                verificarUsuario(role)
                            }

                            "Profesor" -> {
                                verificarUsuario(role)
                            }

                            else -> {
                                // Rol desconocido
                                Toast.makeText(this@Login, "Rol desconocido", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    } else {
                        // El nodo de rol no existe para este usuario
                        Toast.makeText(
                            this@Login,
                            "El usuario no está en la RTBD",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores de lectura de la base de datos
                    Toast.makeText(this@Login, "Error al leer la RTDB", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    private fun verificarUsuario(rol: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val uid = currentUser.uid
        val uRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid)
        uRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val intent = Intent(
                        this@Login,
                        SingUpUsuario::class.java
                    ).apply {
                        putExtra("id", uid)
                        putExtra("correo", currentUser.email)
                        putExtra("rol", rol)
                    }

                    Toast.makeText(this@Login, "Registrando usuario", Toast.LENGTH_SHORT)
                        .show()
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(
                        this@Login,
                        when (rol) {
                            "Admin" -> PanelAdmin::class.java
                            "Tutor" -> MainPanelTutor::class.java
                            else -> MainPanelProfesor::class.java
                        }
                    )
                    startActivity(intent)
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Login, "onCancelled", Toast.LENGTH_SHORT).show()

            }
        })
    }
}