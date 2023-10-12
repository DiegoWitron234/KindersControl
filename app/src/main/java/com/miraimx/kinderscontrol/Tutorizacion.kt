package com.miraimx.kinderscontrol

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.isDigitsOnly
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Tutorizacion : AppCompatActivity(), ModoOscuro {

    private val alumnoLista = mutableListOf<Usuario>()
    private val tutoresLista = mutableListOf<Usuario>()
    private lateinit var lsAlumnoAdapter: ArrayAdapter<Usuario>
    private lateinit var lsTutoresAdapter: ArrayAdapter<Usuario>
    private lateinit var buscarAlumnos: SearchView
    private lateinit var buscarTutores: SearchView
    private lateinit var btnAsignar: Button
    private var posAnteriorAlumno = -1
    private var posAnteriorTutor = -1

    /*data class Usuario(
        val id: String,
        val nombre: String,
        var seleccionado: Boolean,
        var usuario: String
    )*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorizacion)

        cancelarModoOscuro(this)

        buscarAlumnos = findViewById(R.id.buscarAlumno)
        buscarTutores = findViewById(R.id.buscarTutor)
        btnAsignar = findViewById(R.id.btnAsignar)
        btnAsignar.isEnabled = false

        configListView()
        busquedas(buscarAlumnos, buscarTutores)
        btnAsignar()
    }

    private fun configListView() {
        val listAlumno: ListView = findViewById(R.id.spAlumnos)
        val listTutores: ListView = findViewById(R.id.spTutores)
        lsAlumnoAdapter = ListViewUsuarioAdapter(this, alumnoLista)
        lsTutoresAdapter = ListViewUsuarioAdapter(this, tutoresLista)
        listAlumno.adapter = lsAlumnoAdapter
        listTutores.adapter = lsTutoresAdapter
        listAlumno.setOnItemClickListener { _, view, i, _ ->
            val elementoSeleccionado = alumnoLista[i]
            elementoSeleccionado.seleccionado = true

            if (posAnteriorAlumno != -1 && posAnteriorAlumno != i) {
                alumnoLista[posAnteriorAlumno].seleccionado = false
            }
            posAnteriorAlumno = i
            selectLister()
        }

        listTutores.setOnItemClickListener { _, view, i, l ->
            val elementoSeleccionado = tutoresLista[i]
            elementoSeleccionado.seleccionado = true
            if (posAnteriorTutor != -1 && posAnteriorTutor != i) {
                tutoresLista[posAnteriorTutor].seleccionado = false
            }
            posAnteriorTutor = i
            selectLister()
        }
    }


    private fun busquedas(srvAlumno: SearchView, srvTutores: SearchView) {
        srvAlumno.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    if (!p0.isDigitsOnly()) {
                        consulta("alumnos", p0, "matricula", "nombre_alumno", alumnoLista, false)
                    } else {
                        consulta("alumnos", p0, "nombre_alumno", "matricula", alumnoLista, true)
                    }
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
                    consulta("tutores", p0, "tutor_id", "nombre_tutor", tutoresLista, false)
                }
                return true
            }

        })

        srvAlumno.setOnCloseListener {
            // Borrar la lista de alumnos y notificar al adaptador
            alumnoLista.clear()
            lsAlumnoAdapter.notifyDataSetChanged()
            true
        }

        srvTutores.setOnCloseListener {
            // Borrar la lista de tutores y notificar al adaptador
            tutoresLista.clear()
            lsTutoresAdapter.notifyDataSetChanged()
            true
        }
    }

    private fun consulta(
        tabla: String,
        nombre: String,
        atributoId: String,
        atributoNombre: String,
        lista: MutableList<Usuario>,
        orden : Boolean
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
                        val nombreUsuario =
                            usuario.child(atributoNombre).getValue(String::class.java)

                        if (nombreUsuario != null && id != null) {
                            val usuarioDatos = Usuario(
                                if (orden) nombreUsuario else id,
                                if (orden) id else nombreUsuario,
                                false,
                                tabla
                            )
                            lista.add(usuarioDatos)
                        }
                    }
                    lsTutoresAdapter.notifyDataSetChanged()
                    lsAlumnoAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    private fun mostrarConfirmacion() {
        val builder = AlertDialog.Builder(this)
        //val alumnoSeleccion = mutableListOf<String>()
        //val tutorSeleccion = mutableListOf<String>()
        lateinit var tutorSeleccion:String
        lateinit var alumnoSeleccion: String
        builder.setTitle("Confirmación")
        builder.setMessage("¿Desea realizar la asignacion de los alumnos?")
        builder.setPositiveButton("Sí") { dialog, _ ->
            // Aquí puedes agregar la lógica para agregar al niño al tutor
            //Toast.makeText(this, "Niño agregado al tutor", Toast.LENGTH_SHORT).show()
            val tutorizacionRef = FirebaseDatabase.getInstance().getReference("tutorizacion")
           // var tutorizacionInfo = hashMapOf<String, String>()
            //var i = 0
            //var y = 0
            for (alumno in alumnoLista) {
                if (alumno.seleccionado) {
                    //alumnoSeleccion.add(alumno.id)
                    alumnoSeleccion = alumno.id
                }
            }
            for (tutor in tutoresLista) {
                if (tutor.seleccionado) {
                    //tutorSeleccion.add(tutor.id)
                    tutorSeleccion = tutor.id
                }
            }
            tutorizacionRef
                .orderByChild("tutor_id")
                .startAt(tutorSeleccion)
                .endAt(tutorSeleccion + "\uf8ff")
                .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (registros in snapshot.children){
                        if (registros.child("matricula").getValue(String::class.java) == alumnoSeleccion){
                            Toast.makeText(this@Tutorizacion, "El niño ya se encuentra asignado", Toast.LENGTH_SHORT).show()
                            return
                        }
                    }
                    Toast.makeText(this@Tutorizacion, "Asignación exitosa", Toast.LENGTH_SHORT).show()
                    val tutorizacionInfo = hashMapOf(
                        "matricula" to alumnoSeleccion,
                        "tutor_id" to tutorSeleccion
                    )
                    tutorizacionRef.push().setValue(tutorizacionInfo).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //Toast.makeText(this@Tutorizacion, "Operación exitosa", Toast.LENGTH_SHORT).show()
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
                    lsTutoresAdapter.notifyDataSetChanged()
                    lsAlumnoAdapter.notifyDataSetChanged()
                    btnAsignar.isEnabled = false
                    buscarAlumnos.setQuery("", false)
                    buscarTutores.setQuery("", false)
                    dialog.dismiss()
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
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

    private fun btnAsignar() {
        btnAsignar.setOnClickListener {
            mostrarConfirmacion()
        }
    }
}

/*while (i < tutorSeleccion.size) {
    while (y < alumnoSeleccion.size) {
        tutorizacionInfo = hashMapOf(
            "matricula" to alumnoSeleccion[y],
            "tutor_id" to tutorSeleccion[i]
        )
        y++
    }
    i++
}*/