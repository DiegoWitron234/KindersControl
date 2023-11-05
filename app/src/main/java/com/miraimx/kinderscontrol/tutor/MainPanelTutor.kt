package com.miraimx.kinderscontrol.tutor

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.miraimx.kinderscontrol.R
import com.miraimx.kinderscontrol.databinding.ActivityPanelTutorBinding
import com.miraimx.kinderscontrol.cuenta.Login
import com.miraimx.kinderscontrol.cuenta.SingUpTutor

class MainPanelTutor : AppCompatActivity() {
    private lateinit var binding: ActivityPanelTutorBinding
    private val database = FirebaseDatabase.getInstance()

    companion object {
        val PERMISOS = arrayListOf(
            "android.permission.POST_NOTIFICATIONS"
        ).toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPanelTutorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        verificarPermisos()
    }

    override fun onStart() {
        super.onStart()
        verificarUsuario()
    }

    private fun verificarUsuario() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val uRef = database.getReference("tutores").child(uid)
            uRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        val intent = Intent(this@MainPanelTutor, SingUpTutor::class.java)
                        intent.putExtra("correo", currentUser.email)
                        startActivity(intent)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainPanelTutor, "onCancelled", Toast.LENGTH_SHORT).show()
                }
            })
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_superior, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.cerrarSesion -> {
                AlertDialog.Builder(this)
                    .setMessage("¿Seguro que quieres cerrar la sesión?")
                    .setPositiveButton("Salir") { _, _ -> // Acción de confirmación
                        Firebase.auth.signOut()
                        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, Login::class.java)
                        startActivity(intent)
                        finish()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
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