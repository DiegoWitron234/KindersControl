package com.miraimx.kinderscontrol.profesor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.miraimx.kinderscontrol.ControlFirebaseBD
import com.miraimx.kinderscontrol.DatosConsultados
import com.miraimx.kinderscontrol.ModoOscuro
import com.miraimx.kinderscontrol.databinding.FragmentAccesoEntradaBinding
import com.miraimx.kinderscontrol.Usuario
import com.miraimx.kinderscontrol.ListViewUsuarioAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AccesoEntradaFragment : Fragment(), ModoOscuro {

    private lateinit var binding: FragmentAccesoEntradaBinding

    private var listaAlumnos = mutableListOf<Usuario>()

    private var listaMatriculas = mutableListOf<String>()

    private lateinit var listAlumnoAdapter: ArrayAdapter<Usuario>

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

        binding.btnAceptarEntrada.setOnClickListener {
            btnRegistrarAcceso()
        }

        listAlumnoAdapter = ListViewUsuarioAdapter(requireActivity(), listaAlumnos)
        binding.lsCheckAlumno.adapter = listAlumnoAdapter
    }

    private fun cargaDatos(identificador: String) {
        val controlFirebaseBD = ControlFirebaseBD(object : DatosConsultados() {
            override fun onDatosConsulta(resultados: MutableList<String>) {
                super.onDatosConsulta(resultados)
                if (resultados.isNotEmpty()){
                    val matricula = resultados[0]
                    val nombre = resultados[1]
                    //Toast.makeText(requireActivity(), nombre + matricula, Toast.LENGTH_LONG).show()
                    if (listaMatriculas.isNotEmpty()) {
                        listaMatriculas.map { alumno ->
                            if (alumno != matricula) {
                                listaAlumnos.add(
                                    Usuario(
                                        matricula,
                                        nombre,
                                        false
                                    )
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
                        listaAlumnos.add(
                            Usuario(
                                matricula,
                                nombre,
                                false
                            )
                        )
                        listaMatriculas.add(matricula)
                    }
                    listAlumnoAdapter.notifyDataSetChanged()
                }else{
                    Toast.makeText(requireActivity(), "QR invalido", Toast.LENGTH_SHORT).show()
                }

            }
        })
        val query = database.child("alumnos").orderByChild("matricula").equalTo(identificador)
        val listaAtributos = arrayOf("matricula", "nombre_alumno")
        controlFirebaseBD.consultar(query, listaAtributos)
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

    private fun registrar(){
        try {
            val idProfesor = FirebaseAuth.getInstance().currentUser?.uid
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd,HH:mm:ss", Locale.getDefault())
            val fechaHoraActual = dateFormat.format(calendar.time).split(",")

            val scope = CoroutineScope(Dispatchers.IO)

            val jobs = mutableListOf<Job>()

            for (alumno in listaAlumnos) {
                // Crea un mapa para almacenar los datos de este alumno
                val alumnoInfo = hashMapOf(
                    "profesor_id" to idProfesor,
                    "matricula" to alumno.id,
                    "estatus" to "in",
                    "fecha_acceso" to fechaHoraActual[0],
                    "hora_acceso" to fechaHoraActual[1],
                )
                jobs.add(scope.launch {
                    database.child("accesos").push().setValue(alumnoInfo).await()
                })
            }

            for (alumno in listaAlumnos) {
                val alumnoRef = database.child("alumnos/${alumno.id}/accesos")
                val acceso = HashMap<String, Any>()
                acceso["estatus"] = "in"
                acceso["fecha_acceso"] = fechaHoraActual[0]
                acceso["hora_acceso"] = fechaHoraActual[1]
                jobs.add(scope.launch {
                    alumnoRef.updateChildren(acceso).await()
                })
            }

            scope.launch(Dispatchers.Main) {
                jobs.joinAll()
                Toast.makeText(requireActivity(), "Datos guardados", Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigate(
                    AccesoEntradaFragmentDirections.actionAccesoEntradaFragmentPop()
                )
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireActivity(),
                "No se pudo realizar la operación",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}