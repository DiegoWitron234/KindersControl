package com.miraimx.kinderscontrol

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.miraimx.kinderscontrol.databinding.ActivityAsistenciaAlumnosBinding

class AsistenciaAlumnos : AppCompatActivity(), ModoOscuro {
    private val alumnosGrupo = mutableListOf<AccesoAlumno>()
    private lateinit var lsAlumnosGrupoAdapter: ArrayAdapter<AccesoAlumno>
    private lateinit var binding: ActivityAsistenciaAlumnosBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAsistenciaAlumnosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cancelarModoOscuro(this)

        initListaAlumnos()
    }


    private fun initListaAlumnos(){
        lsAlumnosGrupoAdapter = ListViewAccesoAdapter(this, alumnosGrupo)
        binding.listAlumnosGrupo.adapter = lsAlumnosGrupoAdapter

    }

    private fun consultarAsistencia(){
        val firebase = FirebaseDatabase.getInstance().reference
        val ref = firebase.child("checkin").orderByChild("in_out").equalTo("In")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}