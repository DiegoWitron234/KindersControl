package com.miraimx.kinderscontrol.administrador

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.miraimx.kinderscontrol.ControlFirebaseBD
import com.miraimx.kinderscontrol.DatosConsultados
import com.miraimx.kinderscontrol.ListViewUsuarioAdapter
import com.miraimx.kinderscontrol.Propiedades
import com.miraimx.kinderscontrol.R
import com.miraimx.kinderscontrol.Usuario
import com.miraimx.kinderscontrol.databinding.ActivityTutorizacionBinding
import java.util.Locale

class Tutorizacion : AppCompatActivity(), Propiedades {

    private val alumnoLista = mutableListOf<Usuario>()
    private val usuarioLista = mutableListOf<Usuario>()
    private lateinit var lsAlumnoAdapter: ArrayAdapter<Usuario>
    private lateinit var lsTutoresAdapter: ArrayAdapter<Usuario>
    private lateinit var buscarAlumnos: SearchView
    private lateinit var buscarTutores: SearchView
    private lateinit var btnAsignar: Button
    private var posAnteriorAlumno = -1
    private var posAnteriorTutor = -1
    private lateinit var binding: ActivityTutorizacionBinding
    private val database = FirebaseDatabase.getInstance().reference
    private lateinit var tablaUsuario: String
    private lateinit var atributoUsuario: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorizacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cancelarModoOscuro(this)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        tablaUsuario = intent.getStringExtra("usuario").toString()
        binding.tvAsignar.text = intent.getStringExtra("titulo").toString()
        binding.tvBuscarUsuario.text = intent.getStringExtra("subtitulo").toString()

        atributoUsuario = if (tablaUsuario == "tutores") {
            "tutor"
        } else {
            "empleado"
        }

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
        lsTutoresAdapter = ListViewUsuarioAdapter(this, usuarioLista)
        listAlumno.adapter = lsAlumnoAdapter
        listTutores.adapter = lsTutoresAdapter
        listAlumno.setOnItemClickListener { _, _, i, _ ->
            val elementoSeleccionado = alumnoLista[i]
            elementoSeleccionado.seleccionado = true

            if (posAnteriorAlumno != -1 && posAnteriorAlumno != i) {
                alumnoLista[posAnteriorAlumno].seleccionado = false
            }
            posAnteriorAlumno = i
            selectLister()
        }

        listTutores.setOnItemClickListener { _, _, i, _ ->
            val elementoSeleccionado = usuarioLista[i]
            elementoSeleccionado.seleccionado = true
            if (posAnteriorTutor != -1 && posAnteriorTutor != i) {
                usuarioLista[posAnteriorTutor].seleccionado = false
            }
            posAnteriorTutor = i
            selectLister()
        }
    }

    private fun busquedas(srvAlumno: SearchView, srvTutores: SearchView) {
        srvAlumno.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    if (!p0.isDigitsOnly()) {
                        consulta(
                            "alumnos",
                            normalizerText(p0),
                            "matricula",
                            "nombre_alumno",
                            alumnoLista,
                            false
                        )
                    } else {
                        consulta(
                            "alumnos",
                            normalizerText(p0),
                            "nombre_alumno",
                            "matricula",
                            alumnoLista,
                            true
                        )
                    }
                }
                return true
            }

        })

        srvTutores.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    consulta(
                        tablaUsuario,
                        normalizerText(p0),
                        "${atributoUsuario}_id",
                        "nombre_${atributoUsuario}",
                        usuarioLista,
                        false
                    )
                }
                return true
            }
        })
    }


    private fun consulta(
        tabla: String,
        nombre: String,
        atributoId: String,
        atributoBuscar: String,
        lista: MutableList<Usuario>,
        orden: Boolean
    ) {
        val databaseController = ControlFirebaseBD(object : DatosConsultados() {
            override fun onDatosUsuario(resultados: MutableList<Usuario>) {
                lsTutoresAdapter.notifyDataSetChanged()
                lsAlumnoAdapter.notifyDataSetChanged()
            }
        })

        databaseController.consultaTutorizacion(
            tabla,
            nombre,
            atributoId,
            atributoBuscar,
            lista,
            orden
        )
    }

    private fun normalizerText(texto: String): String {
        return texto.split(" ").joinToString(" ") { it ->
            it.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        }
    }

    private fun mostrarConfirmacion() {
        val builder = AlertDialog.Builder(this)
        lateinit var usuarioSeleccion: String
        lateinit var usuarioNombre: String
        lateinit var alumnoSeleccion: String
        lateinit var alumnoNombre: String
        val alumnoRef = database.child("alumnos")
        val datos = hashMapOf<String, Any>()
        var ruta = ""
        var atbUsuario = ""
        builder.setTitle("Confirmación")
        builder.setMessage("¿Desea realizar la asignacion de los alumnos?")
        builder.setPositiveButton("Sí") { dialog, _ ->
            // Aquí puedes agregar la lógica para agregar al niño al tutor
            database.child("tutorizacion")
            for (alumno in alumnoLista) {
                if (alumno.seleccionado) {
                    alumnoSeleccion = alumno.id
                    alumnoNombre = alumno.nombre
                }
            }
            for (usuario in usuarioLista) {
                if (usuario.seleccionado) {
                    usuarioSeleccion = usuario.id
                    usuarioNombre = usuario.nombre
                }
            }


            val controlFirebaseBD = ControlFirebaseBD(object : DatosConsultados() {
                override fun onDatosConsulta(resultados: MutableList<String>) {
                    for (resultado in resultados) {
                        Log.e("Log", resultado)
                        Toast.makeText(this@Tutorizacion, resultado, Toast.LENGTH_SHORT).show()
                        if (resultado == alumnoSeleccion) {
                            Toast.makeText(
                                this@Tutorizacion,
                                "El niño ya se encuentra asignado",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                    }
                    val alumnoRefe = database.child("alumnos")
                    alumnoRefe.child("/$alumnoSeleccion$ruta").updateChildren(datos).addOnCompleteListener{
                        Toast.makeText(this@Tutorizacion, "Alumno asignado", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(this@Tutorizacion, "No se pudo asignar al alumno", Toast.LENGTH_SHORT).show()
                    }
                    alumnoLista.clear()
                    usuarioLista.clear()
                    lsTutoresAdapter.notifyDataSetChanged()
                    lsAlumnoAdapter.notifyDataSetChanged()
                    btnAsignar.isEnabled = false
                    buscarAlumnos.setQuery("", false)
                    buscarTutores.setQuery("", false)
                    dialog.dismiss()
                }
            })

            val query: Query
            if (tablaUsuario == "tutores"){
                ruta = "/tutores"
                datos[usuarioSeleccion] = usuarioNombre
                query = alumnoRef.orderByChild("tutores/${usuarioSeleccion}").equalTo(usuarioNombre)
                atbUsuario = ""
            }else{
                datos["profesor_id"] = usuarioSeleccion
                atbUsuario = "profesor_id"
                query = alumnoRef.orderByChild(atbUsuario).equalTo(usuarioSeleccion)
                Toast.makeText(this@Tutorizacion, "$atbUsuario: $usuarioNombre", Toast.LENGTH_LONG).show()
            }
            controlFirebaseBD.consultar(
                query,
                arrayOf("matricula")
            )

        }
        builder.setNegativeButton("No") { dialog, _ ->
            // Aquí puedes agregar la lógica si el usuario elige "No"
            Toast.makeText(this, "No se agregó al niño al tutor", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun escrituraDatos(
        resultados: MutableList<String>,
        alumnoSeleccion: String,
        alumnoNombre: String,
        usuarioSeleccion: String,
    ) {
        val tutorizacionRef = database.child("tutorizacion")
        for (resultado in resultados) {
            Log.e("Log", resultado)
            if (resultado == alumnoSeleccion) {
                Toast.makeText(
                    this@Tutorizacion,
                    "El niño ya se encuentra asignado",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }
        Toast.makeText(this@Tutorizacion, "Asignación exitosa", Toast.LENGTH_SHORT)
            .show()
        val alumno = hashMapOf(
            "matricula" to alumnoSeleccion,
            "nombre_alumno" to alumnoNombre
        )
        val tutorizacionInfo = hashMapOf<String, Any>(
            "tutor_id" to usuarioSeleccion,
            "alumno" to alumno
        )
        tutorizacionRef.push().setValue(tutorizacionInfo)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(
                        this@Tutorizacion,
                        "No se pudo realizar la operación",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun selectLister() {
        val esAlumnoSeleccionado = alumnoLista.any { it.seleccionado }
        val esTutorSeleccionado = usuarioLista.any { it.seleccionado }
        btnAsignar.isEnabled = esAlumnoSeleccionado && esTutorSeleccionado
    }

    private fun btnAsignar() {
        btnAsignar.setOnClickListener {
            mostrarConfirmacion()
        }
    }

}
