package com.miraimx.kinderscontrol

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PanelAdmin : AppCompatActivity() {

    // Intervalo de tiempo para considerar dos pulsaciones seguidas
    private lateinit var btnAgregarEmpleado: Button
    private lateinit var btnCerrarSesion: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel_admin)

        btnAgregarEmpleado = findViewById(R.id.btnAgregarEmpleado)
        val btnAgregarAlumno = findViewById<Button>(R.id.btnAgregarNino)
        val btnRegistrarEntrada = findViewById<Button>(R.id.btnRegistroEntrada)
        val btnAsignarTutor = findViewById<Button>(R.id.btnAsignarTutorias)

        btnRegistrarEntrada.setOnClickListener{ registroEntrada() }

        btnAsignarTutor.setOnClickListener{ asignarTutor() }

        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        btnAgregarEmpleado.setOnClickListener {
            val intent = Intent(this, RegistrarUsuario::class.java)
            intent.putExtra("rol", "Admin")
            startActivity(intent)
        }

        btnAgregarAlumno.setOnClickListener{
            val intent = Intent(this, SingUpAlumno::class.java)
            startActivity(intent)
        }

        btnCerrarSesion.setOnClickListener { cerrarSesion() }
    }

    private fun verificarUsuario() {
        val database = FirebaseDatabase.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val uRef = database.getReference("empleados").child(uid)
            uRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        val intent = Intent(this@PanelAdmin, SingUpEmpleado::class.java)
                        intent.putExtra("id", uid)
                        intent.putExtra("correo",currentUser.email)
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

    private fun registroEntrada() {
        val database = FirebaseDatabase.getInstance()
        val tutor = "8PalsQD1XmMSEELuEh8x8maxqdv2"
        val alumno = "09876"
        val checkinRef = database.getReference("checkin")
        val nuevoCheck = checkinRef.push()
        val checkID = nuevoCheck.key

        val registroActual = checkinRef.child(tutor).child(alumno)

        // Construye la consulta para obtener el registro más reciente
        val consulta: Query = checkinRef
            .orderByChild("tutor_id")
            .equalTo(tutor)
            .startAt(alumno + "_")
            .endAt(alumno + "_\uf8ff")
            .orderByChild("horafecha_check")
            .limitToLast(1)

        if (checkID != null){
            val estadoInOutRef = checkinRef.child(checkID).child("in_out")

            estadoInOutRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var valorInOut = "In" // Valor predeterminado si no existe un registro anterior

                    if (snapshot.exists()) {
                        valorInOut = snapshot.value.toString()

                        // Cambia el estado de "in" a "out" y viceversa
                        if (valorInOut == "In") {
                            valorInOut = "Out"
                        } else {
                            valorInOut = "In"
                        }
                    }

                    val calendar = Calendar.getInstance()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val fechaHoraActual = dateFormat.format(calendar.time)

                    val checkInfo = hashMapOf(
                        "check_id" to checkID,
                        "matricula" to alumno,
                        "tutor_id" to tutor,
                        "in_out" to valorInOut,
                        "horafecha_check" to fechaHoraActual
                    )

                    checkinRef.child(checkID).setValue(checkInfo).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@PanelAdmin, "El Check-IN fué exitoso", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@PanelAdmin, "No se realizó el Check-IN", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }

    }

    private fun asignarTutor() {
        val database = FirebaseDatabase.getInstance()
        val tutor = "8PalsQD1XmMSEELuEh8x8maxqdv2"
        val alumno = "09876"
        var tutorizacionRef = database.getReference("tutorizacion")
        val nuevaAsignacion = tutorizacionRef.push()
        val asignacionID = nuevaAsignacion.key
        if(asignacionID != null){
            tutorizacionRef = tutorizacionRef.child(asignacionID)
        }

        val tutorizacionInfo = hashMapOf(
            "asignacion_id" to asignacionID,
            "matricula" to alumno,
            "tutor_id" to tutor
            )
        tutorizacionRef.setValue(tutorizacionInfo).addOnCompleteListener{
            task ->
            if (task.isSuccessful){
                Toast.makeText(this, "Asignacion registrada", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "No se pudo realizar la asingacion", Toast.LENGTH_SHORT).show()
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