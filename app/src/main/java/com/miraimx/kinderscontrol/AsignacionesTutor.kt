package com.miraimx.kinderscontrol

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.miraimx.kinderscontrol.databinding.ActivityAsignacionesTutorBinding
import java.text.SimpleDateFormat
import java.util.Date


class AsignacionesTutor : AppCompatActivity(), ModoOscuro {

    private lateinit var binding: ActivityAsignacionesTutorBinding
    val alumnosAccesoLista = mutableListOf<AccesoAlumno>()
    val alumnosAsigLista = mutableListOf<Alumno>()
    private lateinit var lsAccesoAlumnoAdapter: ArrayAdapter<AccesoAlumno>
    private lateinit var lsAsignacionesAlAdapater: ArrayAdapter<Alumno>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAsignacionesTutorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cancelarModoOscuro(this)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        configListas()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            cargarDatos(currentUser.uid)
            cargarDatos2(currentUser.uid)
        }

        intent.getStringExtra("currentId")?.let { cargarDatos(it) }
        intent.getStringExtra("currentId")?.let { cargarDatos2(it) }

        cancelarModoOscuro(this)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configListas() {
        lsAccesoAlumnoAdapter = ListViewAccesoAdapter(this, alumnosAccesoLista)
        lsAsignacionesAlAdapater = ListViewAlumnoAdapter(this, alumnosAsigLista)
        binding.lsRegistrosTutor.adapter = lsAccesoAlumnoAdapter
        binding.lsAsignacionesTutor.adapter = lsAsignacionesAlAdapater
    }

    private fun cargarDatos2(cUserId: String) {
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
                                        alumnosAsigLista.add(
                                            Alumno(
                                                nombreAlumno,
                                                id,
                                            )
                                        )
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
                                                        convertirFecha(tiempo),
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

    @SuppressLint("SimpleDateFormat")
    fun convertirFecha(fechaOriginal : String): String{

        // Formato de entrada
        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        // Formato de salida (day-month-year)
        val formatoSalida = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")

        try {
            // Parsear la fecha original al formato de entrada
            val fechaParseada: Date = formatoEntrada.parse(fechaOriginal)

            // Formatear la fecha al formato de salida
            val fechaFormateada: String = formatoSalida.format(fechaParseada)

            // Imprimir la fecha formateada
            println("Fecha formateada: $fechaFormateada")
            return fechaFormateada
        } catch (e: Exception) {
            println("Error al formatear la fecha: ${e.message}")
            return " "
        }
    }
}

