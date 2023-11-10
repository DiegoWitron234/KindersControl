package com.miraimx.kinderscontrol.administrador

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.miraimx.kinderscontrol.ControlLecturaFirebaseBD
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
    private lateinit var rol: String
    private lateinit var aula: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorizacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val titulo: String
        val subtitulo: String

        cancelarModoOscuro(this)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        rol = intent.getStringExtra("rol").toString()

        if (rol == "Tutor") {
            titulo = "Asignar tutor"
            subtitulo = "Buscar tutor"
        } else {
            titulo = "Asignar profesor"
            subtitulo = "Buscar profesor"
            aula = intent.getStringExtra("grado").toString() + intent.getStringExtra("grupo")
                .toString()
        }
        binding.tvAsignar.text = titulo
        binding.tvBuscarUsuario.text = subtitulo

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

        val textView = TextView(this)
        listTutores.emptyView = textView

        listAlumno.emptyView = textView

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
            val atbAlumnos = arrayOf("nombre_alumno", "apellidos_alumno")

            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    if (!p0.isDigitsOnly()) {
                        consulta(
                            "alumnos",
                            normalizerText(p0),
                            arrayOf("matricula"),
                            atbAlumnos,
                            alumnoLista,
                            false
                        )
                    } else {
                        consulta(
                            "alumnos",
                            normalizerText(p0),
                            atbAlumnos,
                            arrayOf("matricula"),
                            alumnoLista,
                            true
                        )
                    }
                }
                return true
            }
        })

        srvTutores.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            val atbUsuario = arrayOf("nombre_usuario", "apellidos_usuario")
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0 != null) {
                    consulta(
                        "usuarios",
                        normalizerText(p0),
                        arrayOf("usuario_id"),
                        atbUsuario,
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
        atributoId: Array<String>,
        atributoBuscar: Array<String>,
        lista: MutableList<Usuario>,
        orden: Boolean
    ) {
        val databaseController = ControlLecturaFirebaseBD(object : DatosConsultados() {
            override fun onDatosUsuario(resultados: MutableList<Usuario>) {
                lsTutoresAdapter.notifyDataSetChanged()
                lsAlumnoAdapter.notifyDataSetChanged()
            }
        })

        databaseController.consultaTutorizacion(
            tabla,
            nombre,
            rol,
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
        var listaAtributos: Array<String>
        var size = 0
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


            val controlLecturaFirebaseBD = ControlLecturaFirebaseBD(object : DatosConsultados() {
                override fun onDatosConsulta(resultados: MutableList<String>) {
                    for (resultado in resultados) {
                        Log.e("Log", resultado)
                        //Toast.makeText(this@Tutorizacion, resultado, Toast.LENGTH_SHORT).show()
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

                    val mensajeExito = "Alumno asignado"
                    val mensajeFallo = "No se pudo asignar al alumno"
                    val duracionToast = Toast.LENGTH_SHORT

                    alumnoRefe.child("/$alumnoSeleccion$ruta").updateChildren(datos)
                        .addOnCompleteListener {
                            if (ruta != "/tutores") {
                                val datosGrupo = datosGrupo(
                                    usuarioSeleccion,
                                    usuarioNombre
                                )
                                val alumnos = hashMapOf<String, Any>(alumnoSeleccion to alumnoNombre)
                                val grupoRef = database.child("grupos/$aula")
                                grupoRef.updateChildren(datosGrupo)
                                grupoRef.child("/alumnos").updateChildren(alumnos)
                            }
                            Toast.makeText(this@Tutorizacion, mensajeExito, duracionToast).show()
                        }.addOnFailureListener {
                            Toast.makeText(this@Tutorizacion, mensajeFallo, duracionToast).show()
                        }

                    if (size == 2) {
                        if (resultados.size < 2) {
                            alumnoRef.child("/$alumnoSeleccion")
                                .updateChildren(hashMapOf<String, Any>("tutor_main" to usuarioSeleccion))
                                .addOnFailureListener {
                                    Toast.makeText(this@Tutorizacion, mensajeFallo, duracionToast)
                                        .show()
                                }
                        }
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

            if (rol == "Tutor") {
                // Asignar elemento al nodo tutores
                ruta = "/tutores"
                datos[usuarioSeleccion] = usuarioSeleccion
                query = alumnoRef.orderByChild("tutores/${usuarioSeleccion}").equalTo(usuarioSeleccion)
                atbUsuario = ""
                size = 2
                listaAtributos = Array(size) { "" }
                listaAtributos[0] = "matricula"
                listaAtributos[1] = "tutor_main"
            } else {
                // Asignar profesor
                datos["profesor_id"] = usuarioSeleccion
                atbUsuario = "profesor_id"
                query = alumnoRef.orderByChild(atbUsuario).equalTo(usuarioSeleccion)
                size = 1
                listaAtributos = Array(size) { "" }
                listaAtributos[0] = "matricula"
            }
            controlLecturaFirebaseBD.consultar(
                query,
                listaAtributos
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

    private fun selectLister() {
        // Verifica si alguno de los elementos está seleccionado en las listas
        val esAlumnoSeleccionado = alumnoLista.any { it.seleccionado }
        val esTutorSeleccionado = usuarioLista.any { it.seleccionado }
        btnAsignar.isEnabled = esAlumnoSeleccionado && esTutorSeleccionado
    }

    private fun btnAsignar() {
        btnAsignar.setOnClickListener {
            mostrarConfirmacion()
        }
    }

    private fun datosGrupo(
        profesorId: String,
        nombreProfesor: String
    ): HashMap<String, Any> {
        return hashMapOf(
            "profesor_id" to profesorId,
            "nombre_profesor" to nombreProfesor,
            "aula" to aula,
        )
    }

}
