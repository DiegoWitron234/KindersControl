package com.miraimx.kinderscontrol

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class AsignacionesTutor : AppCompatActivity() {
    val alumnosAccesoLista = mutableListOf<AccesoAlumno>()
    private lateinit var lsAccesoAlumnoAdapter: ArrayAdapter<AccesoAlumno>

    val alumnosAsignadosLista = mutableListOf<Usuario>()
    private lateinit var lsAsignacionesAdapter: ArrayAdapter<Usuario>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asignaciones_tutor)

        val lsAsignacionesTutor = findViewById<ListView>(R.id.lsAsignacionesTutor)

        lsAccesoAlumnoAdapter = ListViewAccesoAdapter(this, alumnosAccesoLista)
        lsAsignacionesTutor.adapter = lsAccesoAlumnoAdapter

        lsAsignacionesAdapter = ListViewUsuarioAdapter(this, alumnosAsignadosLista)
        lsAsignacionesTutor.adapter = lsAsignacionesAdapter

        intent.getStringExtra("currentId")?.let { cargarDatos(it) }



        //lsAsignacionesAdapter = listAdapter.Adapter(this, alumnosAsignadosLista, lsAsignacionesTutor)

    }

    private fun cargarDatos(cUserId: String) {
        val database = FirebaseDatabase.getInstance().reference

        val query = database.child("tutorizacion").orderByChild("tutor_id").equalTo(cUserId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var cantidadAlumnos = 0

                for (registro in snapshot.children) {
                    val matricula = registro.child("matricula").getValue(String::class.java)
                    if (matricula != null) {
                        val queryAlumno = database.child("alumnos").child(matricula)
                        queryAlumno.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(alumnoSnapshot: DataSnapshot) {
                                val nombreAlumno = alumnoSnapshot.child("nombre_alumno").getValue(String::class.java)
                                if (nombreAlumno != null) {
                                    val queryCheckin = database.child("checkin").orderByChild("matricula").equalTo(matricula)
                                    queryCheckin.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(checkinSnapshot: DataSnapshot) {
                                            for (checkin in checkinSnapshot.children) {
                                                val estatus = checkin.child("in_out").getValue(String::class.java)
                                                val tiempo = checkin.child("horafecha_check").getValue(String::class.java)
                                                if (estatus != null && tiempo != null) {
                                                    val accesoAlumno = AccesoAlumno(nombreAlumno, estatus, tiempo, matricula)
                                                    Toast.makeText(this@AsignacionesTutor, tiempo + estatus, Toast.LENGTH_SHORT)
                                                        .show()
                                                    alumnosAccesoLista.add(accesoAlumno)
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {}
                                    })
                                }
                                cantidadAlumnos++
                                if (cantidadAlumnos.toLong() == snapshot.childrenCount) {
                                    // Notificar al adaptador sobre los cambios
                                    lsAccesoAlumnoAdapter.notifyDataSetChanged()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun cargarDatosUsuario(cUserId: String) {
        val database = FirebaseDatabase.getInstance().reference
        val query = database.child("tutorizacion").orderByChild("tutor_id").equalTo(cUserId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var cantidadAlumnos = 0
                for (registro in snapshot.children) {
                    val id = registro.child("matricula").getValue(String::class.java)
                    if (id != null) {
                        val queryAlumno =
                            database.child("alumnos").orderByChild("matricula").equalTo(id)
                        queryAlumno.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (alumno in snapshot.children) {
                                    val nombreALumno =
                                        alumno.child("nombre_alumno").getValue(String::class.java)
                                    if (nombreALumno != null) {
                                        val usuarioDatos = Usuario(id, nombreALumno, false, "")
                                        alumnosAsignadosLista.add(usuarioDatos)
                                    }
                                }
                                cantidadAlumnos++
                                if (cantidadAlumnos.toLong() == snapshot.childrenCount) {
                                    // Notificar al adaptador sobre los cambios
                                    lsAsignacionesAdapter.notifyDataSetChanged()
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar la cancelación si es necesario
            }
        })
    }
}






