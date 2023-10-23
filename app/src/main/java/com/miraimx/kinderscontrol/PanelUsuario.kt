package com.miraimx.kinderscontrol

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.miraimx.kinderscontrol.databinding.ActivityPanelUsuarioBinding


class PanelUsuario : AppCompatActivity(), ModoOscuro {

    private lateinit var btnCerrarSesion: Button
    private lateinit var uid: String
    private lateinit var btnMostrarQR: Button
    private lateinit var currentUser: FirebaseUser
    private lateinit var serviceIntent: Intent
    private lateinit var binding: ActivityPanelUsuarioBinding

    companion object {
        val PERMISOS = arrayListOf(
            "android.permission.POST_NOTIFICATIONS"
        ).toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPanelUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cancelarModoOscuro(this)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        binding.btnVerGrupo.setOnClickListener {
            startActivity(Intent(this, AsistenciaAlumnos::class.java))
        }

        verificarPermisos()

        shouldShowRequestPermissionRationale("android.permission.POST_NOTIFICATIONS")

        btnCerrarSesion = binding.btnCerrarSesion
        btnMostrarQR = binding.btnMostrarQR


        btnCerrarSesion.setOnClickListener { cerrarSesion() }

        currentUser = FirebaseAuth.getInstance().currentUser!!

        mostrarAsignaciones()

        btnMostrarQR.setOnClickListener {
            fnMostrarQR()
        }

        if (tienePermisos()) {
            serviceIntent = Intent(this, ServicioOyente::class.java)
            startService(serviceIntent)
        }

        // Agrega un oyente a la referencia de "checkin" en la base de datos
        val checkinRef = FirebaseDatabase.getInstance().getReference("checkin")

        checkinRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Itera a través de todos los hijos de "checkin"
                for (childSnapshot in dataSnapshot.children) {
                    // Obtiene el valor del atributo "tutor_id" de cada registro
                    val tutorId = childSnapshot.child("tutor_id").getValue(String::class.java)

                    // Comprueba si "tutor_id" coincide con tu variable global "uid"
                    if (tutorId == uid) {
                        // Si coincide, muestra un Toast y registra los datos en Logcat
                        val checkinId = childSnapshot.child("check_id").getValue(String::class.java)
                        val horafecha =
                            childSnapshot.child("horafecha_check").getValue(String::class.java)
                        val inOut = childSnapshot.child("in_out").getValue(String::class.java)
                        val matricula =
                            childSnapshot.child("matricula").getValue(String::class.java)

                        // Muestra un Toast
                        //Toast.makeText(applicationContext, "Se agregó un registro con ID: $checkinId", Toast.LENGTH_SHORT).show()

                        // Registra los datos en Logcat
                        Log.d("Registro", "Registro agregado:")
                        Log.d("Registro", "check_id: $checkinId")
                        Log.d("Registro", "horafecha_check: $horafecha")
                        Log.d("Registro", "in_out: $inOut")
                        Log.d("Registro", "matricula: $matricula")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Maneja errores de lectura de la base de datos
                Log.w("Registro", "Error al leer la base de datos: $error", error.toException())
            }
        })
    }

    override fun onStart() {
        super.onStart()
        verificarUsuario()
    }

    private fun mostrarAsignaciones() {
        findViewById<Button>(R.id.btnMostrarNinos).setOnClickListener {
            startActivity(
                Intent(
                    this@PanelUsuario,
                    MainAsignacionActivity::class.java
                )
            )
        }
    }

    private fun cerrarSesion() {
        AlertDialog.Builder(this)
            .setMessage("¿Seguro que quieres cerrar la sesión?")
            .setPositiveButton("Salir") { _, _ -> // Acción de confirmación
                Firebase.auth.signOut()
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                if (tienePermisos()) {
                    stopService(serviceIntent)
                }
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun verificarUsuario() {
        val database = FirebaseDatabase.getInstance()
        uid = currentUser.uid
        val uRef = database.getReference("tutores").child(uid)
        uRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val intent = Intent(this@PanelUsuario, SingUpTutor::class.java)
                    intent.putExtra("correo", currentUser.email)
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PanelUsuario, "onCancelled", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fnMostrarQR() {
        val intent = Intent(this, DisplayQRActivity::class.java)
        intent.putExtra("uid", uid)
        startActivity(intent)
    }

    private fun tienePermisos(): Boolean {
        return PERMISOS.all {
            return ContextCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun verificarPermisos() {
        if (!tienePermisos()) {

            val intent = Intent()
            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"

            //for Android 5-7
            intent.putExtra("app_package", packageName)
            intent.putExtra("app_uid", applicationInfo.uid)

            // for Android 8 and above
            intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)
            startActivity(intent)
        }
    }
}