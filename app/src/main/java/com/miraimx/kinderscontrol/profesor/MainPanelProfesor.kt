package com.miraimx.kinderscontrol.profesor

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.miraimx.kinderscontrol.R
import com.miraimx.kinderscontrol.cuenta.Configuracion
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

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navProfesorFragment) as NavHostFragment
        val navController = navHostFragment.navController
        setupBottomNavMenu(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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
            R.id.configuracion ->{
                startActivity(Intent(this, Configuracion::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupBottomNavMenu(navController: NavController) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomnavprofesor)
        bottomNav?.setupWithNavController(navController)
    }

}