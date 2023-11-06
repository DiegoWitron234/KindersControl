package com.miraimx.kinderscontrol.administrador

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
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
import com.miraimx.kinderscontrol.cuenta.Login
import com.miraimx.kinderscontrol.Propiedades
import com.miraimx.kinderscontrol.R
import com.miraimx.kinderscontrol.cuenta.RegistrarUsuario
import com.miraimx.kinderscontrol.cuenta.SingUpAlumno
import com.miraimx.kinderscontrol.cuenta.SingUpEmpleado
import com.miraimx.kinderscontrol.databinding.ActivityPanelAdminBinding

class PanelAdmin : AppCompatActivity(), Propiedades {

    private lateinit var btnAgregarEmpleado: Button
    private lateinit var binding: ActivityPanelAdminBinding
    private var rolUsuarioRegistrar: String = "Admin"
    private var auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPanelAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cancelarModoOscuro(this)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        btnAgregarEmpleado = binding.btnAgregarEmpleado

        botonDimensiones(
            binding.btnAgregarNino,
            binding.btnRegistroEntrada,
            binding.btnRegistroSalida
        )

        binding.btnRegistroEntrada.setOnClickListener {
            registro("in")
        }

        binding.btnRegistroSalida.setOnClickListener {
            registro("out")
        }
        binding.btnAsignarTutorias.setOnClickListener {
            startActivity(
                Intent(this, Tutorizacion::class.java)
                    .putExtra("usuario", "tutores")
                    .putExtra("titulo", "Asignar tutor")
                    .putExtra("subtitulo", "Buscar tutor")
            )
        }
        binding.btnAsignarGrupo.setOnClickListener {
            startActivity(
                Intent(this, Tutorizacion::class.java)
                    .putExtra("usuario", "empleados")
                    .putExtra("titulo", "Asignar profesor")
                    .putExtra("subtitulo", "Buscar profesor")
            )
        }

        btnAgregarEmpleado.setOnClickListener {
            val intent = Intent(this, RegistrarUsuario::class.java)
            intent.putExtra("rol", "Admin")
            rolUsuarioRegistrar = "Admin"
            startActivity(intent)
        }

        binding.btnAgregarNino.setOnClickListener {
            val intent = Intent(this, SingUpAlumno::class.java)
            startActivity(intent)
        }

        binding.btnAgregarProfesor.setOnClickListener {
            val intent = Intent(this, RegistrarUsuario::class.java)
            intent.putExtra("rol", "Profesor")
            rolUsuarioRegistrar = "Profesor"
            startActivity(intent)
        }
    }
    override fun onStart() {
        super.onStart()
        verificarUsuario()
    }

    /*override fun onDestroy() {
        super.onDestroy()
        if (auth.currentUser != null){
            auth.signOut()
        }
    }*/

    private fun registro(estatus: String) {
        val currentUser = auth.currentUser

        val intent = Intent(this, RegistroAcceso::class.java)
        if (currentUser != null) {
            intent.putExtra("id", currentUser.uid)
            intent.putExtra("estatus", estatus)
            startActivity(intent)
        }
    }

    private fun verificarUsuario() {

        val currentUser = auth.currentUser
        if (currentUser != null) {
            Toast.makeText(this@PanelAdmin, currentUser.email, Toast.LENGTH_LONG).show()
            val uid = currentUser.uid
            val uRef = FirebaseDatabase.getInstance().getReference("empleados").child(uid)
            uRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        val intent = Intent(this@PanelAdmin, SingUpEmpleado::class.java)
                        intent.putExtra("id", uid)
                        intent.putExtra("correo", currentUser.email)
                        intent.putExtra("rol", rolUsuarioRegistrar)
                        startActivity(intent)
                        Toast.makeText(this@PanelAdmin, "Registrando Empleado", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PanelAdmin, "onCancelled", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }



    private fun botonDimensiones(
        btnAgregarAlumno: Button,
        btnRegistrarEntrada: Button,
        btnRegistrarSalida: Button,
    ) {
        val buttons =
            arrayOf(btnAgregarEmpleado, btnAgregarAlumno, btnRegistrarSalida, btnRegistrarEntrada)
        var minTextSize = Float.MAX_VALUE

        for (button in buttons) {
            val textSize = button.textSize
            if (textSize < minTextSize) {
                minTextSize = textSize
            }
        }

        for (button in buttons) {
            button.textSize = minTextSize
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
}
