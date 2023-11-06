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
import com.miraimx.kinderscontrol.ControlFirebaseBD
import com.miraimx.kinderscontrol.DatosConsultados
import com.miraimx.kinderscontrol.ListViewUsuarioAdapter
import com.miraimx.kinderscontrol.Propiedades
import com.miraimx.kinderscontrol.Usuario
import com.miraimx.kinderscontrol.databinding.FragmentAccesoSalidaBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AccesoSalidaFragment : Fragment(), Propiedades {

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let {
            //cargarDatos(it)
            cargarDatosTutor(it)
            idTutor = it
        }
    }
    private lateinit var binding: FragmentAccesoSalidaBinding
    private lateinit var idTutor: String

    // idTutor = "8PalsQD1XmMSEELuEh8x8maxqdv2"
    private val alumnoLista = mutableListOf<Usuario>()

    private lateinit var listViewAdapter: ArrayAdapter<Usuario>

    private val database = FirebaseDatabase.getInstance().reference

    private var posAnteriorAlumno = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccesoSalidaBinding.inflate(inflater, container, false)
        cancelarModoOscuro(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registrar.isEnabled = false

        binding.registrar.setOnClickListener { btnRegistrarAcceso() }

        binding.btnScan.setOnClickListener {
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setPrompt("Leer código QR")
            options.setCameraId(0)
            options.setBeepEnabled(true)
            options.setBarcodeImageEnabled(true)
            barcodeLauncher.launch(options)
        }

        listViewAdapter = ListViewUsuarioAdapter(requireActivity(), alumnoLista)
        binding.lsCheckAlumno.adapter = listViewAdapter
        binding.lsCheckAlumno.setOnItemClickListener { _, _, i, _ ->
            val elementoSeleccionado = alumnoLista[i]
            elementoSeleccionado.seleccionado = true
            if (posAnteriorAlumno != -1 && posAnteriorAlumno != i) {
                alumnoLista[posAnteriorAlumno].seleccionado = false
            }
            posAnteriorAlumno = i
            binding.registrar.isEnabled = true
        }

    }

    private fun cargarDatosTutor(cUserId: String) {
        val query = database.child("tutores").orderByChild("tutor_id").equalTo(cUserId)
        val controlFirebaseBD = ControlFirebaseBD(object : DatosConsultados() {
            override fun onDatosConsulta(resultados: MutableList<String>) {
                super.onDatosConsulta(resultados)
                if (resultados.isNotEmpty()){
                    binding.txtNombraTutorQR.text = "Tutor: $resultados[0]"
                    binding.txtTelefonoTutorQR.text = "Teléfono $resultados[1]"
                    binding.txEmailTutorQR.text = resultados[2]
                    binding.txDireccionTutorQR.text = resultados[3]
                    cargarDatos(cUserId, resultados[0])
                }else{
                    Toast.makeText(requireActivity(), "QR invalido", Toast.LENGTH_SHORT).show()
                }
            }
        })
        val atributos = arrayOf("nombre_tutor", "telefono_tutor", "correo_tutor", "direccion_tutor")
        controlFirebaseBD.consultar(query, atributos)
    }

    private fun cargarDatos(cUserId: String, nombreUsuario: String) {
        val controlFirebaseBD = ControlFirebaseBD(object : DatosConsultados() {
            override fun onDatosUsuario(resultados: MutableList<Usuario>) {
                super.onDatosUsuario(resultados)
                listViewAdapter.notifyDataSetChanged()
            }
        })

        controlFirebaseBD.consultaTutorizaciones(cUserId, nombreUsuario, alumnoLista)
    }


    private fun btnRegistrarAcceso() {
        val idProfesor = FirebaseAuth.getInstance().currentUser?.uid
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Confirmación")
        builder.setMessage("¿Desea realizar la asignación de los alumnos?")

        builder.setPositiveButton("Sí") { _, _ ->
            // Aquí puedes agregar la lógica para agregar al niño al tutor

            var alumnoSeleccion = ""
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd,HH:mm:ss", Locale.getDefault())
            val fechaHoraActual = dateFormat.format(calendar.time).split(",")
            for (alumno in alumnoLista) {
                if (alumno.seleccionado) {
                    alumnoSeleccion = alumno.id
                    break
                }
            }

            val checkInfo = hashMapOf(
                "profesor_id" to idProfesor,
                "matricula" to alumnoSeleccion,
                "estatus" to "out",
                "fecha_acceso" to fechaHoraActual[0],
                "hora_acceso" to fechaHoraActual[1],
                "tutor_id" to idTutor
            )

            // Cargando Datos
            // Se cambió de checkin a acceso
            database.child("accesos").push().setValue(checkInfo).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //Actualizar Datos
                    val alumnoRef = database.child("alumnos/$alumnoSeleccion/accesos")
                    val acceso = HashMap<String, Any>()
                    acceso["estatus"] = "out"
                    acceso["fecha_acceso"] = fechaHoraActual[0]
                    acceso["hora_acceso"] = fechaHoraActual[1]
                    alumnoRef.updateChildren(acceso)

                    /*findNavController().navigate(
                        AccesoSalidaFragmentDirections.actionAccesoSalidaFragmentPop()
                    )*/
                    val pos = binding.lsCheckAlumno.selectedItemPosition
                    binding.lsCheckAlumno.setItemChecked(pos, false)
                    posAnteriorAlumno = -1
                    binding.registrar.isEnabled = false
                    //listViewAdapter.clear()
                    //listViewAdapter.notifyDataSetChanged()
                    binding.registrar
                    Toast.makeText(requireActivity(), "Operación exitosa", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                        requireActivity(),
                        "No se pudo realizar la operación",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            alumnoLista[0].seleccionado = false
        }

        builder.setNegativeButton("No") { dialog, _ ->
            // Aquí puedes agregar la lógica si el usuario elige "No"
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}