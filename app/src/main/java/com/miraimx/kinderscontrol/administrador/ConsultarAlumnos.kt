package com.miraimx.kinderscontrol.administrador

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.miraimx.kinderscontrol.R
import kotlin.math.log

class ConsultarAlumnos : AppCompatActivity() {

    private lateinit var adapter: AlumnoAdapter
    private val alumnos = ArrayList<Alumno>()
    private val alumnosFiltrados = ArrayList<Alumno>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consultar_alumnos)

        val database = FirebaseDatabase.getInstance()
        val alumnosRef = database.getReference("alumnos")

        adapter = AlumnoAdapter(alumnosFiltrados)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                alumnosFiltrados.clear()
                for (alumno in alumnos) {
                    if (alumno.nombre_alumno.toLowerCase().contains(newText.toLowerCase())) {
                        alumnosFiltrados.add(alumno)
                    }
                }
                adapter.notifyDataSetChanged()
                return false
            }
        })

        alumnosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                alumnos.clear()
                for (postSnapshot in dataSnapshot.children) {
                    val alumno = postSnapshot.getValue(Alumno::class.java)
                    if (alumno != null) {
                        alumnos.add(alumno)
                    }
                }
                alumnosFiltrados.clear()
                alumnosFiltrados.addAll(alumnos)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Aquí debes manejar el error
            }
        })
    }

    class AlumnoAdapter(private val alumnos: List<Alumno>) : RecyclerView.Adapter<AlumnoAdapter.AlumnoViewHolder>() {

        class AlumnoViewHolder(val view: View) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlumnoViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return AlumnoViewHolder(view)
        }

        override fun onBindViewHolder(holder: AlumnoViewHolder, position: Int) {
            val alumno = alumnos[position]
            val textView = holder.view.findViewById<TextView>(android.R.id.text1)
            textView.text = alumno.nombre_alumno

            // Obtenemos los datos al dar clic y los guardamos en variables
            holder.itemView.setOnClickListener {
                val nombreAlumno = alumno.nombre_alumno
                val matricula = alumno.matricula
                val edadAlumno = alumno.edad_alumno
                val grado = alumno.grado
                val grupo = alumno.grupo
                val tipoSangreAlumno = alumno.tiposangre_alumno
                val tutores = alumno.tutores.values.toList()
                // Haz algo con las variables aquí
                val intent = Intent(it.context, DatosAlumno::class.java)
                // Pasar datos con putExtra
                intent.putExtra("nombreAlumno", nombreAlumno)
                intent.putExtra("matricula", matricula)
                intent.putExtra("edadAlumno", edadAlumno)
                intent.putExtra("gradoGrupo", grado+grupo)
                intent.putExtra("tipoSangreAlumno", tipoSangreAlumno)
                intent.putStringArrayListExtra("tutores", ArrayList(tutores))

                // Iniciar la Activity
                it.context.startActivity(intent)
            }
        }

        override fun getItemCount() = alumnos.size
    }
}

data class Alumno(
    val nombre_alumno: String = "",
    val matricula: String = "",
    val edad_alumno: String = "",
    val grado: String = "",
    val grupo: String = "",
    val tiposangre_alumno: String = "",
    val tutores: Map<String, String> = emptyMap()
)

