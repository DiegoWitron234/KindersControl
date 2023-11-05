package com.miraimx.kinderscontrol.profesor

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.miraimx.kinderscontrol.cuenta.Login
import com.miraimx.kinderscontrol.cuenta.SingUpEmpleado
import com.miraimx.kinderscontrol.databinding.ActivityPanelProfesorBinding

class MainPanelProfesor : AppCompatActivity() {

    private lateinit var binding: ActivityPanelProfesorBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPanelProfesorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    override fun onStart() {
        super.onStart()
        verificarUsuario()
    }

    private fun verificarUsuario() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val uRef = FirebaseDatabase.getInstance().getReference("empleados").child(uid)
            uRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        val intent = Intent(this@MainPanelProfesor, SingUpEmpleado::class.java)
                        intent.putExtra("id", uid)
                        intent.putExtra("correo", currentUser.email)
                        intent.putExtra("rol", "Profesor")
                        startActivity(intent)
                        Toast.makeText(this@MainPanelProfesor, "Registrando Empleado", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainPanelProfesor, "onCancelled", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
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
}