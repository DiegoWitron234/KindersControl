package com.miraimx.kinderscontrol.cuenta

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.miraimx.kinderscontrol.ModoOscuro
import com.miraimx.kinderscontrol.databinding.ActivityRegistrarUsuarioBinding

class RegistrarUsuario : AppCompatActivity(), ModoOscuro {

    private lateinit var auth: FirebaseAuth
    private lateinit var campoNuevoCorreo: EditText
    private lateinit var campoNuevaPassword: EditText
    private lateinit var campoConfirmarPassword: EditText
    private lateinit var btnRegistrarse: Button
    private lateinit var txtCorreoValido: TextView
    private lateinit var txtPassValida: TextView
    private lateinit var checkboxAceptar: CheckBox
    private lateinit var binding: ActivityRegistrarUsuarioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        cancelarModoOscuro(this)

        val rol = intent.getStringExtra("rol")
        if (rol == null) {
            Toast.makeText(this, "El rol es desconocido", Toast.LENGTH_LONG).show()
            finish()
        }

        auth = Firebase.auth

        campoNuevoCorreo = binding.campoNuevoCorreo
        campoNuevaPassword = binding.campoNuevaContraseA
        campoConfirmarPassword = binding.campoConfirmarContraseA
        btnRegistrarse = binding.btnRegistrarse
        txtCorreoValido = binding.txtCorreoValido
        txtPassValida = binding.txtContraseAValida
        btnRegistrarse.isEnabled = false

        campoNuevoCorreo.addTextChangedListener(watcher)
        campoNuevaPassword.addTextChangedListener(watcher)
        campoConfirmarPassword.addTextChangedListener(watcher)

        checkboxAceptar = binding.checkboxAceptar

        val termsAndPrivacyTextView = binding.termsAndPrivacyTextView

        val message =
            "Confirma que has leído y aceptas los términos y condiciones y el aviso de privacidad."

        val spannableString = SpannableString(message)

        val termsClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                openWebPage("https://drive.google.com/file/d/1bRcUV6G8qncZ0i-nLLMNvfMKFbNok2UB/view?usp=sharing")
            }
        }

        val privacyClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                openWebPage("https://drive.google.com/file/d/188t2xfCTc9XaVemcxVrBvnrCuvA5QEcn/view?usp=drive_link")
            }
        }

        spannableString.setSpan(termsClickableSpan, 37, 59, 0)
        spannableString.setSpan(privacyClickableSpan, 65, 84, 0)

        termsAndPrivacyTextView.text = spannableString
        termsAndPrivacyTextView.movementMethod = LinkMovementMethod.getInstance()

        btnRegistrarse.setOnClickListener {
            val correo = campoNuevoCorreo.text.toString()
            val password = campoNuevaPassword.text.toString()

            if (checkboxAceptar.isChecked) {
                if (rol != null) {
                    crearCuenta(correo, password, rol)
                }
            } else {
                Toast.makeText(this, "Debe aceptar los términos y condiciones", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun openWebPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private val watcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        @SuppressLint("SetTextI18n")
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val correoValido = isValidEmail(campoNuevoCorreo.text.toString())
            val contrasenaValida = campoNuevaPassword.text.length >= 6
            val contrasenasCoinciden =
                campoNuevaPassword.text.toString() == campoConfirmarPassword.text.toString()

            if (correoValido) {
                txtCorreoValido.text = ""
            } else {
                txtCorreoValido.text = "Ingrese un correo válido"
            }
            if (!contrasenaValida) {
                txtPassValida.text = "La contraseña debe tener al menos 6 caracteres"
            } else if (!contrasenasCoinciden) {
                txtPassValida.text = "Las contraseñas no coinciden"
            } else
                txtPassValida.text = ""
            btnRegistrarse.isEnabled = correoValido && contrasenaValida && contrasenasCoinciden
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun crearCuenta(email: String, password: String, rol: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    Toast.makeText(this, "Usuario registrado", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                    if (user != null) {
                        val uid = user.uid
                        val database = FirebaseDatabase.getInstance()
                        val usersRef = database.getReference("users")
                        val userData = hashMapOf(
                            "role" to rol
                        )
                        usersRef.child(uid).setValue(userData)
                        finish()
                    }
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Error de autenticación.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
}