package com.miraimx.kinderscontrol

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.miraimx.kinderscontrol.databinding.FragmentAsignacionBinding
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class AsignacionFragment : Fragment(), ModoOscuro {

    private lateinit var binding: FragmentAsignacionBinding
    private lateinit var lsAsignacionesAlAdapater: ArrayAdapter<Alumno>
    val alumnosAsigLista = mutableListOf<Alumno>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAsignacionBinding.inflate(inflater, container, false)
        cancelarModoOscuro(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configListas()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            alumnosAsigLista.clear()
            cargarDatos(currentUser.uid)
        }
    }

    private fun configListas() {
        lsAsignacionesAlAdapater = ListViewAlumnoAdapter(requireContext(), alumnosAsigLista)
        binding.lsAsignacionesTutor.adapter = lsAsignacionesAlAdapater
        binding.lsAsignacionesTutor.setOnItemClickListener { _, _, i, _ ->
            val nombre = alumnosAsigLista[i].nombre
            val matricula = alumnosAsigLista[i].matricula
            val edad = alumnosAsigLista[i].edad
            val sangre = alumnosAsigLista[i].tipoSangre
            val aula = alumnosAsigLista[i].aula

            findNavController().navigate(
                AsignacionFragmentDirections.actionAsignacionFragmentToAlumnoFragment(
                    datosAlumno = arrayOf(nombre, matricula, edad, sangre, aula)
                )
            )
        }
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
                                    val nombreAlumno =
                                        alumno.child("nombre_alumno").getValue(String::class.java)
                                    val edad =
                                        alumno.child("edad_alumno").getValue(String::class.java)
                                    val tipoSangre = alumno.child("tiposangre_alumno")
                                        .getValue(String::class.java)
                                    val aula =
                                        alumno.child("grado_grupo").getValue(String::class.java)
                                    if (nombreAlumno != null && aula != null && edad != null && tipoSangre != null) {
                                        val queryCheck =
                                            database.child("checkin").orderByChild("matricula")
                                                .equalTo(id).limitToLast(1)
                                        queryCheck.addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            @RequiresApi(Build.VERSION_CODES.O)
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                for (checkin in snapshot.children) {
                                                    var estatus = checkin.child("in_out")
                                                        .getValue(String::class.java)
                                                    val tiempo = checkin.child("horafecha_check")
                                                        .getValue(String::class.java)
                                                    if (estatus != null && tiempo != null) {
                                                        estatus = if (estatus == "in") {
                                                            "Ingresó"
                                                        } else {
                                                            "Salió"
                                                        }
                                                        alumnosAsigLista.add(
                                                            Alumno(
                                                                nombreAlumno,
                                                                id,
                                                                aula,
                                                                edad,
                                                                tipoSangre,
                                                                estatus,
                                                                convertirFecha(tiempo)
                                                            )
                                                        )
                                                        lsAsignacionesAlAdapater.notifyDataSetChanged()
                                                    }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                            }

                                        })

                                        //lsAsignacionesAlAdapater.notifyDataSetChanged()
                                    }
                                }
                                cantidadAlumnos++
                                if (cantidadAlumnos.toLong() == snapshot.childrenCount) {
                                    // Notificar al adaptador sobre los cambios
                                    lsAsignacionesAlAdapater.notifyDataSetChanged()
                                    binding.lsAsignacionesTutor.invalidate()
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertirFecha(fechaOriginal: String): String {
        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
        val formatoFechaHora = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy - hh:mm a", Locale("es"))

        return try {
            val fechaParseada: Date = formatoEntrada.parse(fechaOriginal) as Date
            val fechaLocal = fechaParseada.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            val fechaFormateada: String = formatoFechaHora.format(fechaLocal)
            fechaFormateada
        } catch (e: Exception) {
            println("Error al formatear la fecha: ${e.message}")
            " "
        }
    }

}