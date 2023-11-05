package com.miraimx.kinderscontrol.tutor

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.miraimx.kinderscontrol.Alumno
import com.miraimx.kinderscontrol.ControlFirebaseBD
import com.miraimx.kinderscontrol.DatosConsultados
import com.miraimx.kinderscontrol.ListViewAlumnoAdapter
import com.miraimx.kinderscontrol.ModoOscuro
import com.miraimx.kinderscontrol.databinding.FragmentTutorPanelBinding

class TutorPanelFragment : Fragment(), ModoOscuro {

    private lateinit var binding: FragmentTutorPanelBinding
    private lateinit var lsAsignacionesAlAdapater: ArrayAdapter<Alumno>
    private val alumnosAsigLista = mutableListOf<Alumno>()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val dbRef = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =FragmentTutorPanelBinding.inflate(inflater, container, false)
        cancelarModoOscuro(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configListas()
        if (currentUser != null) {
            alumnosAsigLista.clear()

            datosTutor(currentUser.uid)

            binding.btnMostrarQR.setOnClickListener {
                startActivity(
                    Intent(requireActivity(), DisplayQRActivity::class.java)
                        .putExtra("uid", currentUser.uid)
                )
            }
        }
    }

    private fun configListas() {
        lsAsignacionesAlAdapater = ListViewAlumnoAdapter(requireContext(), alumnosAsigLista)
        binding.lsAsignacionesTutor.adapter = lsAsignacionesAlAdapater
        binding.lsAsignacionesTutor.setOnItemClickListener { _, _, i, _ ->
            val nombre = alumnosAsigLista[i].nombre
            val matricula = alumnosAsigLista[i].matricula
            val edad = alumnosAsigLista[i].edad
            val sangre = alumnosAsigLista[i].tipoSangre
            val aula = alumnosAsigLista[i].grado + alumnosAsigLista[i].grupo

            findNavController().navigate(
                TutorPanelFragmentDirections.actionAsignacionFragmentToAlumnoFragment(
                    datosAlumno = arrayOf(nombre, matricula, edad, sangre, aula)
                )
            )
        }
    }

    private fun cargarDatos(claveUsuario: String, nombreTutor: String) {
        val controlFirebaseBD = ControlFirebaseBD(object : DatosConsultados() {
            override fun onDatosAlumno(resultados: MutableList<Alumno>) {
                super.onDatosAlumno(resultados)
                lsAsignacionesAlAdapater.notifyDataSetChanged()
            }
        })
        val query =
            dbRef.child("alumnos").orderByChild("tutores/$claveUsuario").equalTo(nombreTutor)
        controlFirebaseBD.consultaAsignacion(query, alumnosAsigLista)
    }

    private fun datosTutor(uid: String) {
        val controlFirebaseBD = ControlFirebaseBD(object : DatosConsultados() {
            override fun onDatosConsulta(resultados: MutableList<String>) {
                super.onDatosConsulta(resultados)
                val nombreTutor = resultados[0]
                binding.nombreTutor.text = nombreTutor
                cargarDatos(uid, nombreTutor)
            }
        })
        val query =
            dbRef.child("tutores").orderByChild("tutor_id").equalTo(uid)
        val datos = arrayOf("nombre_tutor")
        controlFirebaseBD.consultar(query, datos)
    }
}