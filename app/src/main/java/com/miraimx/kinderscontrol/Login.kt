package com.miraimx.kinderscontrol

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var campoCorreo: EditText
    private lateinit var campoContrasena: EditText
    private lateinit var btnLogin: Button
    private lateinit var textRegistrar: TextView
    private lateinit var txtCorreoValido: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        /*
        val actionBar: ActionBar? = supportActionBar


        if (actionBar != null) {
            //Poner el ícono al ActionBar
            actionBar.setIcon(R.mipmap.ic_launcher_foreground)
            actionBar.setDisplayShowHomeEnabled(true)
        }
         */

        auth = Firebase.auth

        campoCorreo = findViewById(R.id.editCorreo)
        campoContrasena = findViewById(R.id.editContrasena)
        btnLogin = findViewById(R.id.btnLogin)
        textRegistrar = findViewById(R.id.textRegistrar)
        txtCorreoValido = findViewById(R.id.txtCorreoValido)

        btnLogin.isEnabled = false


        // Agregar el TextWatcher al campo de correo electrónico
        campoCorreo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val correoValido = s.isValidEmail()
                if (correoValido){
                    txtCorreoValido.setText("")
                }else{
                    txtCorreoValido.setText("Ingrese un correo válido")
                }
                btnLogin.isEnabled = correoValido && campoContrasena.text.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Listener al campo de contraseña para habilitar o deshabilitar el botón de inicio de sesión
        campoContrasena.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                btnLogin.isEnabled = campoCorreo.text.isValidEmail() && s?.isNotEmpty() == true
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Listener al botón de inicio de sesión (btnLogin) para realizar la acción de inicio de sesión
        btnLogin.setOnClickListener {
            // Lógica del Login
            val correo: String = campoCorreo.text.toString()
            val contraseña: String = campoContrasena.text.toString()
            println("Correo: $correo \nContraseña: $contraseña")
            signIn(correo, contraseña)
        }

        // Listener para registrar un nuevo usuario
        textRegistrar.setOnClickListener {
            // Intent para ir a la activity de registro de usuario
            val intent= Intent(this, RegistrarUsuario::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // Función para validar el formato del correo electrónico
    private fun CharSequence?.isValidEmail(): Boolean {
        if (this == null || isEmpty()) return false
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
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
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

}