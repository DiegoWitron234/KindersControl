package com.miraimx.kinderscontrol

import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.miraimx.kinderscontrol.databinding.ActivityAsistenciaAlumnosBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AsistenciaAlumnos : AppCompatActivity(), ModoOscuro {
    private val alumnosGrupo = mutableListOf<AccesoAlumno>()
    private lateinit var lsAlumnosGrupoAdapter: ArrayAdapter<AccesoAlumno>
    private lateinit var binding: ActivityAsistenciaAlumnosBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAsistenciaAlumnosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cancelarModoOscuro(this)
        MobileAds.initialize(
            this
        ) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        initListaAlumnos()
    }


    private fun initListaAlumnos() {
        lsAlumnosGrupoAdapter = ListViewAccesoAdapter(this, alumnosGrupo)
        binding.listAlumnosGrupo.adapter = lsAlumnosGrupoAdapter
        consultarAsistencia()
        if (lsAlumnosGrupoAdapter.count == 0){
            val mensaje = "Sin registros el día de hoy 😴"

            AlertDialog.Builder(this).apply {
                setTitle("Mensaje")
                setMessage(mensaje)
                setPositiveButton("OK") { dialog, _ ->
                    finish()
                    dialog.dismiss()
                }
                setOnCancelListener {
                    finish()
                }
                show()
            }

        }
    }

    private fun consultarAsistencia() {
        val fechaActual = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now()
        } else {
            Calendar.getInstance().time.toString()
        }
        val firebase = FirebaseDatabase.getInstance().reference
        val refCheck = firebase.child("checkin").orderByChild("in_out").equalTo("in")
        refCheck.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ch in snapshot.children) {
                    val fecha = ch.child("horafecha_check").getValue(String::class.java)
                    //Toast.makeText(this@AsistenciaAlumnos, "REGISTRO DE HORA OBTENIDO", Toast.LENGTH_SHORT).show()
                    if (!fecha.isNullOrEmpty()) {
                        //Toast.makeText(this@AsistenciaAlumnos, "NO ES NULO", Toast.LENGTH_SHORT).show()
                        //Toast.makeText(this@AsistenciaAlumnos, "Hoy: "+fechaActual+ "registro: "+convertirFecha(fecha), Toast.LENGTH_SHORT).show()
                        if (fechaActual.toString() == convertirFecha(fecha)) {
                            //Toast.makeText(this@AsistenciaAlumnos, "REGISTROS ENCONTRADOS", Toast.LENGTH_SHORT).show()
                            val matricula = ch.child("matricula").getValue(String::class.java)
                            val refAlumnos =
                                firebase.child("alumnos").orderByChild("matricula")
                                    .equalTo(matricula)
                            refAlumnos.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (a in snapshot.children) {
                                        val nombre =
                                            a.child("nombre_alumno").getValue(String::class.java)
                                        //Toast.makeText(this@AsistenciaAlumnos, matricula + ": " + nombre, Toast.LENGTH_SHORT).show()
                                        if (!nombre.isNullOrEmpty() && !matricula.isNullOrEmpty()) {
                                            //Toast.makeText(this@AsistenciaAlumnos, "REGISTROS REGISTRADOS", Toast.LENGTH_SHORT).show()
                                            val alumno = AccesoAlumno(
                                                nombre,
                                                fecha,
                                                matricula,
                                                matricula
                                            )
                                            alumnosGrupo.add(alumno)
                                            lsAlumnosGrupoAdapter.notifyDataSetChanged()
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }

                            })
                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    fun convertirFecha(fechaOriginal: String): String {

        // Formato de entrada
        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)

        // Formato de salida (day-month-year)
        val formatoSalida = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)

        return try {
            // Parsear la fecha original al formato de entrada
            val fechaParseada: Date = formatoEntrada.parse(fechaOriginal) as Date

            // Formatear la fecha al formato de salida
            val fechaFormateada: String = formatoSalida.format(fechaParseada)

            // Imprimir la fecha formateada
            //println("Fecha formateada: $fechaFormateada")
            fechaFormateada
        } catch (e: Exception) {
            println("Error al formatear la fecha: ${e.message}")
            " "
        }
    }
}