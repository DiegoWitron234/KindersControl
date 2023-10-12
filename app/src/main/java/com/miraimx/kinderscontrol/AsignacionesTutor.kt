package com.miraimx.kinderscontrol

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AsignacionesTutor : AppCompatActivity(), ModoOscuro {
    val alumnosAccesoLista = mutableListOf<AccesoAlumno>()
    val alumnosAsigLista = mutableListOf<Alumno>()
    private lateinit var lsAccesoAlumnoAdapter: ArrayAdapter<AccesoAlumno>
    private lateinit var lsAsignacionesAlAdapater: ArrayAdapter<Alumno>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asignaciones_tutor)
        val lsRegistrosTutor = findViewById<ListView>(R.id.lsRegistrosTutor)
        val lsAsignacionesTutor = findViewById<ListView>(R.id.lsAsignacionesTutor)

        lsAccesoAlumnoAdapter = ListViewAccesoAdapter(this, alumnosAccesoLista)
        lsAsignacionesAlAdapater = ListViewAlumnoAdapter(this, alumnosAsigLista )
        lsRegistrosTutor.adapter = lsAccesoAlumnoAdapter
        lsAsignacionesTutor.adapter = lsAsignacionesAlAdapater

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null){
            cargarDatos(currentUser.uid)
            cargarDatos2(currentUser.uid)
        }

        intent.getStringExtra("currentId")?.let { cargarDatos(it) }
        intent.getStringExtra("currentId")?.let { cargarDatos2(it) }

        cancelarModoOscuro(this)
    }

    private fun cargarDatos2(cUserId: String){
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
                                    val nombreAlumno =
                                        alumno.child("nombre_alumno").getValue(String::class.java)
                                    if (nombreAlumno != null) {
                                        val alumno = Alumno(
                                            nombreAlumno,
                                            id,
                                        )
                                        alumnosAsigLista.add(alumno)
                                    }
                                }
                                cantidadAlumnos++
                                if (cantidadAlumnos.toLong() == snapshot.childrenCount) {
                                    // Notificar al adaptador sobre los cambios
                                    lsAsignacionesAlAdapater.notifyDataSetChanged()
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
                                val nombreAlumno = alumnoSnapshot.child("nombre_alumno")
                                    .getValue(String::class.java)
                                if (nombreAlumno != null) {
                                    val queryCheckin =
                                        database.child("checkin").orderByChild("matricula")
                                            .equalTo(matricula).limitToLast(1)
                                    queryCheckin.addListenerForSingleValueEvent(object :
                                        ValueEventListener {
                                        override fun onDataChange(checkinSnapshot: DataSnapshot) {
                                            for (checkin in checkinSnapshot.children) {
                                                val estatus = checkin.child("in_out")
                                                    .getValue(String::class.java)
                                                val tiempo = checkin.child("horafecha_check")
                                                    .getValue(String::class.java)
                                                if (estatus != null && tiempo != null) {
                                                    val accesoAlumno = AccesoAlumno(
                                                        nombreAlumno,
                                                        tiempo,
                                                        estatus,
                                                        matricula
                                                    )
                                                    alumnosAccesoLista.add(accesoAlumno)
                                                    lsAccesoAlumnoAdapter.notifyDataSetChanged()
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

}






