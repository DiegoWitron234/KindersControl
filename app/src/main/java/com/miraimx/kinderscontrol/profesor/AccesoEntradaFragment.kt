package com.miraimx.kinderscontrol.profesor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.miraimx.kinderscontrol.ControlLecturaFirebaseBD
import com.miraimx.kinderscontrol.DatosConsultados
import com.miraimx.kinderscontrol.Propiedades
import com.miraimx.kinderscontrol.databinding.FragmentAccesoEntradaBinding
import com.miraimx.kinderscontrol.Usuario
import com.miraimx.kinderscontrol.ListViewUsuarioAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AccesoEntradaFragment : Fragment(), Propiedades {

    private lateinit var binding: FragmentAccesoEntradaBinding

    private var listaAlumnos = mutableListOf<Usuario>()

    private var listaMatriculas = mutableListOf<String>()

    private lateinit var listAlumnoAdapter: ArrayAdapter<Usuario>

    val idProfesor = FirebaseAuth.getInstance().currentUser?.uid

    private val database = FirebaseDatabase.getInstance().reference

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let {
            cargaDatos(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccesoEntradaBinding.inflate(inflater, container, false)
        cancelarModoOscuro(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnScan.setOnClickListener {
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setPrompt("Leer código QR")
            options.setCameraId(0)
            options.setBeepEnabled(true)
            options.setBarcodeImageEnabled(true)
            barcodeLauncher.launch(options)
        }

        binding.btnAceptarEntrada.isEnabled = false

        binding.btnAceptarEntrada.setOnClickListener {
            btnRegistrarAcceso()
        }

        listAlumnoAdapter = ListViewUsuarioAdapter(requireActivity(), listaAlumnos)
        binding.lsCheckAlumno.adapter = listAlumnoAdapter
    }

    private fun cargaDatos(identificador: String) {
        val controlLecturaFirebaseBD = ControlLecturaFirebaseBD(object : DatosConsultados() {
            override fun onDatosConsulta(resultados: MutableList<String>) {
                super.onDatosConsulta(resultados)
                if (resultados.isNotEmpty()) {
                    binding.btnAceptarEntrada.isEnabled = true
                    val matricula = resultados[0]
                    val nombre = resultados[1]
                    val apellidos = resultados[2]
                    if (idProfesor == resultados[3]) {
                       // Toast.makeText(requireActivity(), nombre + apellidos, Toast.LENGTH_LONG)
                        if (listaMatriculas.isNotEmpty()) {
                            listaMatriculas.map { alumno ->
                                if (alumno != matricula) {
                                    listaAlumnos.add(
                                        Usuario(matricula, "$nombre $apellidos", false, "")
                                    )
                                    listaMatriculas.add(matricula)
                                    Toast.makeText(
                                        requireActivity(),
                                        "Alumno registrado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        requireActivity(),
                                        "El alumno ya se encuentra en la lista",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            listaAlumnos.add(Usuario(matricula, "$nombre $apellidos", false, ""))
                            listaMatriculas.add(matricula)
                            //obtenerTutores(matricula)
                        }
                    }
                    listAlumnoAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireActivity(), "QR invalido", Toast.LENGTH_SHORT).show()
                }

            }
        })
        val query = database.child("alumnos").orderByChild("matricula").equalTo(identificador)
        val listaAtributos =
            arrayOf("matricula", "nombre_alumno", "apellidos_alumno", "profesor_id")
        controlLecturaFirebaseBD.consultar(query, listaAtributos)
    }

    private fun btnRegistrarAcceso() {

        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Confirmación")
        builder.setMessage("¿Desea realizar la asignación de los alumnos?")

        builder.setPositiveButton("Sí") { _, _ ->
            registrar()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun registrar() {
        try {
            val idProfesor = FirebaseAuth.getInstance().currentUser?.uid
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd,HH:mm:ss", Locale.getDefault())
            val fechaHoraActual = dateFormat.format(calendar.time).split(",")

            val scope = CoroutineScope(Dispatchers.IO)

            val jobs = mutableListOf<Job>()

            val tutores = HashMap<String?, String?>()

            for (alumno in listaAlumnos) {
                tutores.clear()
                obtenerNodos(alumno.id, object : Callback {
                    override fun onCallback(value: MutableList<Pair<String?, String?>>) {
                        for (tutor in value) {
                            tutores[tutor.first] = tutor.second
                        }
                        val alumnoInfo = hashMapOf(
                            "profesor_id" to idProfesor,
                            "matricula" to alumno.id,
                            "tutores" to tutores,
                            "estatus" to "in",
                            "fecha_acceso" to fechaHoraActual[0],
                            "hora_acceso" to fechaHoraActual[1],
                        )
                        jobs.add(scope.launch {
                            try {
                                database.child("accesos").push().setValue(alumnoInfo).await()
                            } catch (e: Exception) {
                                //
                            }
                        })
                    }
                })
            }
            val acceso = HashMap<String, Any>()
            for (alumno in listaAlumnos) {
                acceso.clear()
                val alumnoRef = database.child("alumnos/${alumno.id}/accesos")
                acceso["estatus"] = "in"
                acceso["fecha_acceso"] = fechaHoraActual[0]
                acceso["hora_acceso"] = fechaHoraActual[1]
                jobs.add(scope.launch {
                    try {
                        alumnoRef.setValue(acceso).await()
                    } catch (e: Exception) {
                        //
                    }
                })
            }

            scope.launch(Dispatchers.Main) {
                jobs.joinAll()
                listaAlumnos.clear()
                listaMatriculas.clear()
                listAlumnoAdapter.notifyDataSetChanged()
                binding.btnAceptarEntrada.isEnabled = false
                Toast.makeText(requireActivity(), "Datos guardados", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireActivity(),
                "No se pudo realizar la operación",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun obtenerNodos(matricula: String, callback: Callback) {
        val query = database.child("alumnos/${matricula}/tutores")
        val controlLecturaFirebaseBD = ControlLecturaFirebaseBD(object : DatosConsultados() {})
        controlLecturaFirebaseBD.consultarNodos(query) { listaTutores ->
            callback.onCallback(listaTutores)
        }
    }

    interface Callback {
        fun onCallback(value: MutableList<Pair<String?, String?>>)
    }
}