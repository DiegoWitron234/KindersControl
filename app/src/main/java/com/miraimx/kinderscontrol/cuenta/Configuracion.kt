package com.miraimx.kinderscontrol.cuenta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.miraimx.kinderscontrol.R
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class Configuracion : AppCompatActivity() {

    private lateinit var textViewCambiarCorreo: TextView
    private lateinit var textViewCambiarContraseña: TextView
    private lateinit var textViewBorrarCuenta: TextView
    private lateinit var textViewCambiarDatos: TextView
    private lateinit var campoNuevoCorreo: EditText
    private lateinit var campoConfirmarCorreo: EditText
    private lateinit var botonGuardar: Button
    private lateinit var campoNuevaContraseña: EditText
    private lateinit var campoConfirmarContraseña: EditText
    private lateinit var botonEliminar: Button
    private lateinit var txtAviso: TextView

    private lateinit var campoCorreo: EditText
    private lateinit var campoContrasena: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuracion)

        val actionBar = supportActionBar
        actionBar?.setIcon(R.mipmap.ic_launcher_foreground)
        actionBar?.title = "Configuración"
        actionBar?.setDisplayShowHomeEnabled(true)

        textViewCambiarCorreo = findViewById(R.id.textViewCambiarCorreo)
        textViewCambiarContraseña = findViewById(R.id.textViewCambiarContrasena)
        textViewBorrarCuenta = findViewById(R.id.textViewEliminarCuenta)
        textViewCambiarDatos = findViewById(R.id.textViewCambiarDatos)


        textViewCambiarCorreo.setOnClickListener {
            setContentView(R.layout.cambiar_correo)

            campoNuevoCorreo = findViewById(R.id.campoNuevoCorreo)
            campoConfirmarCorreo = findViewById(R.id.campoConfirmarCorreo)
            botonGuardar = findViewById(R.id.btnGuardar)
            campoCorreo = findViewById(R.id.campoCorreoActual)
            campoContrasena = findViewById(R.id.campoContraseña)
            txtAviso = findViewById(R.id.txtAviso)

            botonGuardar.isEnabled = false

            // Agregar TextWatcher a campoCorreo
            campoCorreo.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    validarCampos()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // Agregar TextWatcher a campoNuevoCorreo
            campoNuevoCorreo.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    validarCampos()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // Agregar TextWatcher a campoConfirmarCorreo
            campoConfirmarCorreo.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    validarCampos()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // Agregar TextWatcher a campoContraseña
            campoContrasena.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    validarCampos()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            botonGuardar.setOnClickListener {
                val user = Firebase.auth.currentUser

                // Reautenticar al usuario antes de cambiar el correo electrónico
                val email = campoCorreo.text.toString()
                val password = campoContrasena.text.toString()
                val credentials = EmailAuthProvider.getCredential(email, password)
                val uid = user?.uid

                user?.reauthenticate(credentials)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // La reautenticación fue exitosa, el usuario está autenticado nuevamente.
                            // Ahora podemos cambiar el correo electrónico.

                            val database = FirebaseDatabase.getInstance()
                            val myRef = database.getReference("usuarios")
                            val nuevoCorreo = campoNuevoCorreo.text.toString()

                            if (uid != null) {
                                myRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            // uid encontrado, agregamos el correo_usuario
                                            myRef.child(uid).child("correo_usuario").setValue(nuevoCorreo)
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        // Error al leer los datos
                                        println("Error al leer los datos: " + databaseError.code)
                                    }
                                })
                            }


                            user.updateEmail(nuevoCorreo)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        Log.d(TAG, "User email address updated.")
                                        Toast.makeText(this, "Correo actualizado", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        // Mostrar un mensaje de error en caso de que la actualización falle
                                        Toast.makeText(this, "Error al actualizar el correo.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            // La reautenticación falló.
                            Toast.makeText(this, "Los datos son incorrectos.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        textViewCambiarContraseña.setOnClickListener {
            setContentView(R.layout.cambiar_contrasena)

            campoNuevaContraseña = findViewById(R.id.campoNuevaContraseña)
            campoConfirmarContraseña = findViewById(R.id.campoConfirmarContraseña)
            botonGuardar = findViewById(R.id.btnGuardar)
            campoCorreo = findViewById(R.id.campoCorreoActual)
            campoContrasena = findViewById(R.id.campoContraseña)
            txtAviso = findViewById(R.id.txtAviso)

            botonGuardar.isEnabled = false

            // Agregar TextWatcher a campoCorreo
            campoCorreo.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    validarCamposCambiarContra()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // Agregar TextWatcher a campoNuevoCorreo
            campoNuevaContraseña.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    validarCamposCambiarContra()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // Agregar TextWatcher a campoConfirmarCorreo
            campoConfirmarContraseña.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    validarCamposCambiarContra()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // Agregar TextWatcher a campoContraseña
            campoContrasena.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    validarCamposCambiarContra()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            botonGuardar.setOnClickListener {
                val user = Firebase.auth.currentUser

                // Reautenticar al usuario antes de cambiar el correo electrónico
                val email = campoCorreo.text.toString()
                val password = campoContrasena.text.toString()
                val credentials = EmailAuthProvider.getCredential(email, password)

                user?.reauthenticate(credentials)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // La reautenticación fue exitosa, el usuario está autenticado nuevamente.
                            // Ahora podemos cambiar la contraseña.
                            val newPassword = campoNuevaContraseña.text.toString()

                            user!!.updatePassword(newPassword)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d(TAG, "User password updated.")
                                        Toast.makeText(this, "Contraseña cambiada", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                    if (task.isCanceled) {
                                        Log.d(TAG, "User password canceled.")
                                        Toast.makeText(this, "Falló el cambio de contraseña", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            // La reautenticación falló.
                            Toast.makeText(this, "Los datos son incorrectos.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            }
        }

        textViewBorrarCuenta.setOnClickListener {
            setContentView(R.layout.eliminar_cuenta)

            campoContrasena = findViewById(R.id.campoContraseña)
            campoCorreo = findViewById(R.id.campoCorreoActual)
            botonEliminar = findViewById(R.id.btnEliminar)
            txtAviso = findViewById(R.id.txtAviso)

            botonEliminar.isEnabled = false

            // Agregar TextWatcher a campoCorreo
            campoCorreo.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    validarCamposEliminarCuenta()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // Agregar TextWatcher a campoContraseña
            campoContrasena.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    validarCamposEliminarCuenta()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            botonEliminar.setOnClickListener {
                // Mostrar el cuadro de diálogo de confirmación
                AlertDialog.Builder(this)
                    .setMessage("¿Seguro que quieres eliminar tu cuenta?")
                    .setPositiveButton("Sí") { dialog, which -> // Acción de confirmación
                        val user = Firebase.auth.currentUser

                        // Reautenticar al usuario antes de cambiar el correo electrónico
                        val email = campoCorreo.text.toString()
                        val password = campoContrasena.text.toString()
                        val credentials = EmailAuthProvider.getCredential(email, password)
                        val uid = user?.uid



                        user?.reauthenticate(credentials)
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // La reautenticación fue exitosa, el usuario está autenticado nuevamente.
                                    // Ahora podemos eliminar la cuenta-

                                    //Cambiamos el estado en la RTDB a false

                                    val database = FirebaseDatabase.getInstance()
                                    val myRef = database.getReference("users")

                                    if (uid != null) {
                                        myRef.child(uid).addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    // uid encontrado, cambiamos el estado a false
                                                    myRef.child(uid).child("estado").setValue(false)
                                                    Log.w("Mensaje", "Estado de RTDB cambiado.")
                                                    println("Estado de la RTDB cambiado")
                                                    Toast.makeText(
                                                        this@Configuracion,
                                                        "Cuenta inhabilitada de la RTDB",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {
                                                // Error al leer los datos
                                                println("Error al leer los datos: " + databaseError.code)
                                                Toast.makeText(
                                                    this@Configuracion,
                                                    "No se pudo inhabilitar de la RTDB 2",
                                                    Toast.LENGTH_LONG
                                                )
                                                    .show()
                                            }
                                        })
                                    }

                                    //Eliminamos el usuario en Auth
                                    user.delete()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Log.d(TAG, "Cuenta de usuario Auth eliminada.")

                                                Toast.makeText(this, "Cuenta eliminada exitosamente.", Toast.LENGTH_SHORT).show()
                                                // Cerrar todas las demás actividades y volver a MainActivity
                                                val intent = Intent(this, Login::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                                startActivity(intent)
                                                finish()
                                            }
                                            if (task.isCanceled) {
                                                Log.d(TAG, "User account canceled.")
                                                Toast.makeText(this, "Falló la eliminación.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                } else {
                                    // La reautenticación falló.
                                    Toast.makeText(this, "Los datos son incorrectos.", Toast.LENGTH_SHORT).show()
                                }
                            }

                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        textViewCambiarDatos.setOnClickListener {
            val user = Firebase.auth.currentUser
            val uid = user?.uid
            val database = FirebaseDatabase.getInstance()

            // Verificar si el usuario está autenticado
            if (uid != null) {
                val userRef = database.getReference("usuarios").child(uid)

                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val role = dataSnapshot.child("rol").value.toString()
                            val intent = Intent(
                                this@Configuracion,
                                SingUpUsuario::class.java
                            ).apply {
                                putExtra("id", uid)
                                putExtra("correo", user.email)
                                putExtra("rol", role)
                            }
                            startActivity(intent)
                            }
                        else {
                            // El nodo de rol no existe para este usuario
                            Toast.makeText(
                                this@Configuracion,
                                "El usuario no está en la RTBD",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Manejar errores de lectura de la base de datos
                        Toast.makeText(this@Configuracion, "Error al leer la RTDB", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    // Función para validar los campos y habilitar/deshabilitar el botón Guardar cuando cambiamos correo.
    private fun validarCampos() {
        val correoActual = campoCorreo.text.toString()
        val nuevoCorreo = campoNuevoCorreo.text.toString()
        val confirmarCorreo = campoConfirmarCorreo.text.toString()
        val contraseña = campoContrasena.text.toString()

        // Verificar si todos los campos son válidos y cumplen las condiciones
        val sonCorreosValidos = isValidEmail(correoActual) && isValidEmail(nuevoCorreo) && isValidEmail(confirmarCorreo)
        val coincidenCorreos = nuevoCorreo == confirmarCorreo
        val tieneContraseñaValida = contraseña.length >= 6

        if (!sonCorreosValidos){
            txtAviso.setText("Ingrese correos válidos")
        }else if (!coincidenCorreos){
            txtAviso.setText("Los correos no coinciden")
        } else if (!tieneContraseñaValida){
            txtAviso.setText("La contraseña debe tener al menos 6 caracteres")
        } else
            txtAviso.setText("")

        botonGuardar.isEnabled = sonCorreosValidos && coincidenCorreos && tieneContraseñaValida
    }

    //Función para validar los campos y habilitat/deshabilitar el botón Guardar cuando cambiamos contraseña.
    private fun validarCamposCambiarContra() {
        val correoActual = campoCorreo.text.toString()
        val nuevaContraseña = campoNuevaContraseña.text.toString()
        val confirmarContraseña = campoConfirmarContraseña.text.toString()
        val contraseña = campoContrasena.text.toString()

        // Verificar si todos los campos son válidos y cumplen las condiciones
        val esCorreoVálido = isValidEmail(correoActual)
        val coincidenContraseñas = nuevaContraseña == confirmarContraseña
        val tieneContraseñaValida = contraseña.length >= 6 && nuevaContraseña.length >= 6 && confirmarContraseña.length >= 6

        if (!esCorreoVálido){
            txtAviso.setText("Ingrese un correo válido")
        } else if (!tieneContraseñaValida){
            txtAviso.setText("La contraseña debe tener al menos 6 caracteres")
        }else if (!coincidenContraseñas){
            txtAviso.setText("Las contraseñas no coinciden")
        } else
            txtAviso.setText("")

        botonGuardar.isEnabled = esCorreoVálido && coincidenContraseñas && tieneContraseñaValida
    }

    private fun validarCamposEliminarCuenta() {
        val correoActual = campoCorreo.text.toString()
        val contraseña = campoContrasena.text.toString()

        // Verificar si todos los campos son válidos y cumplen las condiciones
        val esCorreoValido = isValidEmail(correoActual)
        val tieneContraseñaValida = contraseña.length >=6

        if (!esCorreoValido){
            txtAviso.setText("Ingrese un correo válido")
        } else if (!tieneContraseñaValida){
            txtAviso.setText("La contraseña debe tener al menos 6 caracteres")
        } else
            txtAviso.setText("")

        botonEliminar.isEnabled = esCorreoValido && tieneContraseñaValida
    }

    // Función para verificar si un texto es un correo electrónico válido
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}