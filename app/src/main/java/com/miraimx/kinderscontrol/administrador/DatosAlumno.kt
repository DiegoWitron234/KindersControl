package com.miraimx.kinderscontrol.administrador

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.miraimx.kinderscontrol.R
import com.miraimx.kinderscontrol.ControlFirebaseStg

class DatosAlumno : AppCompatActivity() {

    private lateinit var fotoAlumno: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_alumno)

        val controlFirebaseStg = ControlFirebaseStg()
        fotoAlumno = findViewById(R.id.fotoAlumno)

        // Obtener los datos del Intent
        val nombreAlumno = intent.getStringExtra("nombreAlumno")
        val matricula = intent.getStringExtra("matricula")
        val edadAlumno = intent.getStringExtra("edadAlumno")
        val gradoGrupo = intent.getStringExtra("gradoGrupo")
        val tipoSangreAlumno = intent.getStringExtra("tipoSangreAlumno")
        val tutores = intent.getStringArrayListExtra("tutores") ?: ArrayList()

        // Mostrar los datos en los TextViews
        val txtNombre = findViewById<TextView>(R.id.txtNombre)
        val txtMatricula = findViewById<TextView>(R.id.txtMatricula)
        val txtEdad = findViewById<TextView>(R.id.txtEdad)
        val txtGrupo = findViewById<TextView>(R.id.txtGrupo)
        val txtTipoSangre = findViewById<TextView>(R.id.txtTipoSangre)

        txtNombre.text = "Nombre: $nombreAlumno"
        txtMatricula.text = "Matricula: $matricula"
        txtEdad.text = "Edad: $edadAlumno"
        txtGrupo.text = "Grupo: $gradoGrupo"
        txtTipoSangre.text = "Tipo de sangre: $tipoSangreAlumno"

        controlFirebaseStg.cargarImagen(
            "alumnos/$matricula.png",
            fotoAlumno,
            this
        )

        // Mostrar los tutores en el ListView
        val listaTutores = findViewById<ListView>(R.id.listaTutores)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tutores)
        listaTutores.adapter = adapter
    }
}