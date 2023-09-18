package com.miraimx.kinderscontrol

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Integer.min

class Tutorizacion : AppCompatActivity() {

    private val alumnoLista = mutableListOf<Usuario>()
    private val tutoresLista = mutableListOf<Usuario>()
    private lateinit var recyclerAdapterAlumnos: RecyclerViewAdapter
    private lateinit var recyclerAdapterTutores: RecyclerViewAdapter
    private lateinit var buscarAlumnos: SearchView
    private lateinit var buscarTutores: SearchView

    private lateinit var btnAsignar: Button


    data class Usuario(
        val id: String,
        val nombre: String,
        var seleccionado: Boolean,
        var usuario: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorizacion)
        buscarAlumnos = findViewById(R.id.buscarAlumno)
        buscarTutores = findViewById(R.id.buscarTutor)
        val recyclerTutores: RecyclerView = findViewById(R.id.rvTutores)
        val recyclerAlumnos: RecyclerView = findViewById(R.id.rvAlumnos)

        btnAsignar = findViewById(R.id.btnAsignar)
        btnAsignar.isEnabled = false

        initRecyclerView(recyclerAlumnos, recyclerTutores)
        busquedas(buscarAlumnos, buscarTutores)
        btnAsignar()
    }

    private fun busquedas(srvAlumno: SearchView, srvTutores: SearchView) {
        srvAlumno.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    consulta("alumnos", p0, "matricula", "nombre_alumno", alumnoLista)
                }
                return true
            }

        })

        srvTutores.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    consulta("tutores", p0, "tutor_id", "nombre_tutor", tutoresLista)
                }
                return true
            }

        })

        srvAlumno.setOnCloseListener {
            // Borrar la lista de alumnos y notificar al adaptador
            alumnoLista.clear()
            recyclerAdapterAlumnos.notifyDataSetChanged()
            true
        }

        srvTutores.setOnCloseListener {
            // Borrar la lista de tutores y notificar al adaptador
            tutoresLista.clear()
            recyclerAdapterTutores.notifyDataSetChanged()
            true
        }
    }
    private fun consulta(
        tabla: String,
        nombre: String,
        atributoId: String,
        atributoNombre: String,
        lista: MutableList<Usuario>
    ) {
        if (nombre.isNotBlank()) {
            val database = FirebaseDatabase.getInstance().reference.child(tabla)
            val alumnosQuery =
                database.orderByChild(atributoNombre).startAt(nombre).endAt(nombre + "\uf8ff")
            alumnosQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista.clear() // Borra la lista antes de agregar nuevos resultados
                    for (usuario in snapshot.children) {
                        val id = usuario.child(atributoId).getValue(String::class.java)
                        val nombreUsuario = usuario.child(atributoNombre).getValue(String::class.java)
                        if (nombreUsuario != null && id != null) {
                            val usuarioDatos = Usuario(id, nombreUsuario, false, tabla)
                            lista.add(usuarioDatos)
                        }
                    }
                    recyclerAdapterAlumnos.notifyDataSetChanged()
                    recyclerAdapterTutores.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    private fun initRecyclerView(ryAlumnos: RecyclerView, ryTutores: RecyclerView) {
        val managerAlumnos = LinearLayoutManager(this)
        val managerTutores = LinearLayoutManager(this)
        //val observerAlumno = AdapterObserver(ryAlumnos)
        //val observerTutores = AdapterObserver(ryTutores)

        ryAlumnos.layoutManager = managerAlumnos
        recyclerAdapterAlumnos = RecyclerViewAdapter(alumnoLista) { selectLister() }
        //recyclerAdapterAlumnos.registerAdapterDataObserver(observerAlumno)
        ryAlumnos.adapter = recyclerAdapterAlumnos

        ryTutores.layoutManager = managerTutores
        recyclerAdapterTutores = RecyclerViewAdapter(tutoresLista) { selectLister() }
        //recyclerAdapterTutores.registerAdapterDataObserver(observerTutores)
        ryTutores.adapter = recyclerAdapterTutores

        //recyclerAdapterAlumnos.notifyDataSetChanged()
        //recyclerAdapterTutores.notifyDataSetChanged()
    }


    private fun mostrarConfirmacion() {
        val builder = AlertDialog.Builder(this)
        val alumnoSeleccion = mutableListOf<String>()
        val tutorSeleccion = mutableListOf<String>()
        builder.setTitle("Confirmación")
        builder.setMessage("¿Desea realizar la asignacion de los alumnos?")
        builder.setPositiveButton("Sí") { dialog, _ ->
            // Aquí puedes agregar la lógica para agregar al niño al tutor
            Toast.makeText(this, "Niño agregado al tutor", Toast.LENGTH_SHORT).show()
            val tutorizacionRef = FirebaseDatabase.getInstance().getReference("tutorizacion").push()
            var tutorizacionInfo = hashMapOf<String, String>()
            var i = 0
            var y = 0
            for (alumno in alumnoLista) {
                if (alumno.seleccionado) {
                    alumnoSeleccion.add(alumno.id)
                }
            }
            for (tutor in tutoresLista) {
                if (tutor.seleccionado) {
                    tutorSeleccion.add(tutor.id)
                }
            }
            while (i < tutorSeleccion.size) {
                while (y < alumnoSeleccion.size) {
                    tutorizacionInfo = hashMapOf(
                        "matricula" to alumnoSeleccion[y],
                        "tutor_id" to tutorSeleccion[i]
                    )
                    y++
                }
                i++
            }
            tutorizacionRef.setValue(tutorizacionInfo).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@Tutorizacion, "Operación exitosa", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                        this@Tutorizacion,
                        "No se pudo realizar la operación",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
           alumnoLista.clear()
            tutoresLista.clear()
            recyclerAdapterAlumnos.notifyDataSetChanged()
            recyclerAdapterTutores.notifyDataSetChanged()
            btnAsignar.isEnabled = false
            buscarAlumnos.setQuery("", false)
            buscarTutores.setQuery("", false)
            dialog.dismiss()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            // Aquí puedes agregar la lógica si el usuario elige "No"
            Toast.makeText(this, "No se agregó al niño al tutor", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun selectLister() {
        val esAlumnoSeleccionado = alumnoLista.any { it.seleccionado }
        val esTutorSeleccionado = tutoresLista.any { it.seleccionado }
        btnAsignar.isEnabled = esAlumnoSeleccionado && esTutorSeleccionado
    }

    private fun eliminarTutorizacion(){
       // val database = FirebaseDatabase.getInstance().reference.child(tabla)
    }

    private fun btnAsignar() {
        btnAsignar.setOnClickListener {
            mostrarConfirmacion()
        }
    }
}

