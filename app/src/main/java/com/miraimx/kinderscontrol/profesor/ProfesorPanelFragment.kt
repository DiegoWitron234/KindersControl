package com.miraimx.kinderscontrol.profesor

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.miraimx.kinderscontrol.Alumno
import com.miraimx.kinderscontrol.ControlFirebaseBD
import com.miraimx.kinderscontrol.DatosConsultados
import com.miraimx.kinderscontrol.ListViewAlumnoAdapter
import com.miraimx.kinderscontrol.Propiedades
import com.miraimx.kinderscontrol.R
import com.miraimx.kinderscontrol.databinding.FragmentProfesorPanelBinding

class ProfesorPanelFragment : Fragment(), Propiedades {

    private lateinit var binding: FragmentProfesorPanelBinding
    private val database = FirebaseDatabase.getInstance().reference
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val listaAlumnos = mutableListOf<Alumno>()
    private lateinit var listViewAlumnoAdapter: ListViewAlumnoAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfesorPanelBinding.inflate(inflater, container, false)
        cancelarModoOscuro(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listViewAlumnoAdapter = ListViewAlumnoAdapter(requireActivity(), listaAlumnos)
        binding.listviewGrupo.adapter = listViewAlumnoAdapter
        binding.listviewGrupo.divider = ColorDrawable(Color.TRANSPARENT)
        binding.listviewGrupo.dividerHeight = 10

        if (currentUser != null){
            listaAlumnos.clear()
            cargaDatos()
        }

    }

    private fun cargaDatos() {
        val controlFirebaseBD = ControlFirebaseBD(object : DatosConsultados() {

            override fun onDatosConsulta(resultados: MutableList<String>) {
                super.onDatosConsulta(resultados)
                if (resultados.isNotEmpty()){
                    val nombre = resultados[0]
                    val apellidos = resultados[1]
                    binding.txtNombreProfesor.text =
                        getString(R.string.nombre_empleado, nombre, apellidos)
                }
            }

            override fun onDatosAlumno(resultados: MutableList<Alumno>) {
                super.onDatosAlumno(resultados)
                listViewAlumnoAdapter.notifyDataSetChanged()
            }
        })
        datosProfesor(controlFirebaseBD)
        datosGrupo(controlFirebaseBD)
    }

    private fun datosProfesor(controlFirebaseBD: ControlFirebaseBD) {
        val query =
            database.child("empleados").orderByChild("empleado_id").equalTo(currentUser?.uid)
        val datos = arrayOf("nombre_empleado", "apellidos_empleado")
        controlFirebaseBD.consultar(query, datos)
    }

    private fun datosGrupo(controlFirebaseBD: ControlFirebaseBD){
        val query = database.child("alumnos").orderByChild("profesor_id").equalTo(currentUser?.uid)
        controlFirebaseBD.consultaAsignacion(query, listaAlumnos)
    }

}