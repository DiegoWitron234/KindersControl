package com.miraimx.kinderscontrol

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
        var seleccionado: Boolean
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
                recyclerAdapterAlumnos.notifyDataSetChanged()
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                alumnoLista.clear()
                if (p0 != null) {
                    Toast.makeText(this@Tutorizacion, p0, Toast.LENGTH_SHORT).show()
                    consulta("alumnos", p0, "matricula", "nombre_alumno")
                }
                recyclerAdapterAlumnos.notifyDataSetChanged()
                return true
            }

        })

        srvTutores.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                recyclerAdapterTutores.notifyDataSetChanged()
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                tutoresLista.clear()
                if (p0 != null){
                    Toast.makeText(this@Tutorizacion, p0, Toast.LENGTH_SHORT).show()
                    consulta("tutores", p0, "tutor_id", "nombre_tutor")
                }
                recyclerAdapterTutores.notifyDataSetChanged()
                return true
            }

        })
    }

    private fun consulta(tabla: String, nombre: String, atributoId: String, atributoNombre: String) {
        if (nombre.isNotBlank()){
            val database = FirebaseDatabase.getInstance().reference.child(tabla)
            val alumnosQuery =
                database.orderByChild(atributoNombre).startAt(nombre).endAt(nombre + "\uf8ff")
            alumnosQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (usuario in snapshot.children) {
                        val id = usuario.child(atributoId).getValue(String::class.java)
                        val nombre = usuario.child(atributoNombre).getValue(String::class.java)
                        if (nombre != null && id != null) {
                            val usuarioDatos = Usuario(id, nombre, false)
                            if (atributoId == "matricula"){
                                alumnoLista.add(usuarioDatos)
                            }else{
                                tutoresLista.add(usuarioDatos)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

    }

    private fun initRecyclerView(ryAlumnos: RecyclerView, ryTutores: RecyclerView) {
        val managerAlumnos = LinearLayoutManager(this)
        val managerTutores = LinearLayoutManager(this)
        ryAlumnos.layoutManager = managerAlumnos
        recyclerAdapterAlumnos = RecyclerViewAdapter(alumnoLista) { selectLister() }
        ryAlumnos.adapter = recyclerAdapterAlumnos
        ryTutores.layoutManager = managerTutores
        recyclerAdapterTutores = RecyclerViewAdapter(tutoresLista) { selectLister() }
        ryTutores.adapter = recyclerAdapterTutores
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
            for (alumno in alumnoLista){
                if (alumno.seleccionado){
                    alumnoSeleccion.add(alumno.id)
                }
            }
            for (tutor in tutoresLista){
                if (tutor.seleccionado){
                    tutorSeleccion.add(tutor.id)
                }
            }
             while (i < tutorSeleccion.size){
                while (y < alumnoSeleccion.size){
                    tutorizacionInfo = hashMapOf(
                        "matricula" to alumnoSeleccion[y],
                        "tutor_id" to tutorSeleccion[i]
                    )
                    y++
                }
                 i++
            }
            tutorizacionRef.setValue(tutorizacionInfo).addOnCompleteListener{
                task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@Tutorizacion, "Operación exitosa", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@Tutorizacion, "No se pudo realizar la operación", Toast.LENGTH_SHORT).show()
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
        var esAlumno = false
        var esTutor = false
        for (alumno in alumnoLista) {
            if (alumno.seleccionado) {
                esAlumno= true
                break
            }
        }
        for (alumno in tutoresLista) {
            if (alumno.seleccionado) {
                esTutor= true
                break
            }
        }
        btnAsignar.isEnabled = esAlumno && esTutor
    }

    private fun btnAsignar() {
        btnAsignar.setOnClickListener {
            mostrarConfirmacion()
        }
    }

}

