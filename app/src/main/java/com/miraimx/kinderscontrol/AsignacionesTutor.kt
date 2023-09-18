package com.miraimx.kinderscontrol

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AsignacionesTutor : AppCompatActivity() {
    val alumnosAsignados = mutableListOf<Usuario>()
    private lateinit var recyclerAsignacionAdapter: RecyclerAdapterAsignacion

    data class Usuario(
        val id: String,
        val nombre: String,
        var seleccionado: Boolean
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asignaciones_tutor)
        val lvAsignacionesTutor = findViewById<RecyclerView>(R.id.lvAsignacionesTutor)
        val manager = LinearLayoutManager(this)
        val decoracion = DividerItemDecoration(this, manager.orientation)
        lvAsignacionesTutor.layoutManager = manager
        recyclerAsignacionAdapter =
            com.miraimx.kinderscontrol.RecyclerAdapterAsignacion(alumnosAsignados)
        lvAsignacionesTutor.adapter = recyclerAsignacionAdapter
        lvAsignacionesTutor.addItemDecoration(decoracion)

        intent.getStringExtra("currentId")?.let { cargarDatos(it) }
    }

    private fun cargarDatos(cUserId: String) {
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
                                        val usuarioDatos = Usuario(id, nombreALumno, false)
                                        alumnosAsignados.add(usuarioDatos)
                                    }
                                }
                                cantidadAlumnos++
                                if (cantidadAlumnos.toLong() == snapshot.childrenCount) {
                                    // Notificar al adaptador sobre los cambios
                                    recyclerAsignacionAdapter.notifyDataSetChanged()
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




