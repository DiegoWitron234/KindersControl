package com.miraimx.kinderscontrol

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PanelAdmin : AppCompatActivity() {

    private lateinit var btnAgregarEmpleado: Button
    private lateinit var btnCerrarSesion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_admin)

        btnAgregarEmpleado = findViewById(R.id.btnAgregarEmpleado)
        val btnAgregarAlumno = findViewById<Button>(R.id.btnAgregarNino)
        val btnRegistrarEntrada = findViewById<Button>(R.id.btnRegistroEntrada)
        val btnRegistrarSalida = findViewById<Button>(R.id.btnRegistroSalida)
        val btnAsignarTutor = findViewById<Button>(R.id.btnAsignarTutorias)

        //btnRegistrarEntrada.setOnClickListener { registro("In") }
        btnRegistrarEntrada.setOnClickListener {
            val intent = Intent(this, LeerQR_Activity::class.java)
            startActivity(intent)
        }

        btnRegistrarSalida.setOnClickListener { registro("Out") }
        btnAsignarTutor.setOnClickListener { startActivity(Intent(this, Tutorizacion::class.java)) }

        btnAgregarEmpleado.setOnClickListener {
            val intent = Intent(this, RegistrarUsuario::class.java)
            intent.putExtra("rol", "Admin")
            startActivity(intent)
        }

        btnAgregarAlumno.setOnClickListener {
            val intent = Intent(this, SingUpAlumno::class.java)
            startActivity(intent)
        }

        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        btnCerrarSesion.setOnClickListener { cerrarSesion() }


    }



    private fun verificarUsuario() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val uRef = FirebaseDatabase.getInstance().getReference("empleados").child(uid)
            uRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        val intent = Intent(this@PanelAdmin, SingUpEmpleado::class.java)
                        intent.putExtra("id", uid)
                        intent.putExtra("correo", currentUser.email)
                        startActivity(intent)
                        Toast.makeText(this@PanelAdmin, "Registrando Empleado", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this@PanelAdmin, "Existe", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@PanelAdmin, "onCancelled", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun registro(estado: String) {
        val tutor = "8PalsQD1XmMSEELuEh8x8maxqdv2"
        val alumno = "09876"
        var empleado = ""
        val empleadoInstancia = FirebaseAuth.getInstance().currentUser
        if(empleadoInstancia != null){
            empleado = empleadoInstancia.uid
        }
        val checkinRef = FirebaseDatabase.getInstance().getReference("checkin").push()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fechaHoraActual = dateFormat.format(calendar.time)
        val checkInfo = hashMapOf(
            "matricula" to alumno,
            "tutor_id" to tutor,
            "empleado_id" to empleado,
            "in_out" to estado,
            "horafecha_check" to fechaHoraActual
        )
        guardarRegistroEnFirebase(checkinRef, checkInfo)
    }

    private fun asignarTutor() {
        val tutor = "8PalsQD1XmMSEELuEh8x8maxqdv2"
        val alumno = "09876"
        val tutorizacionRef = FirebaseDatabase.getInstance().getReference("tutorizacion").push()
        val tutorizacionInfo = hashMapOf(
            "matricula" to alumno,
            "tutor_id" to tutor
        )
        guardarRegistroEnFirebase(tutorizacionRef, tutorizacionInfo)
    }

    private fun guardarRegistroEnFirebase(ref: DatabaseReference, data: Map<String, Any>) {
        ref.setValue(data).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@PanelAdmin, "Operación exitosa", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@PanelAdmin, "No se pudo realizar la operación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cerrarSesion() {
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
    }

    override fun onStart() {
        super.onStart()
        verificarUsuario()
    }
}
