package com.miraimx.kinderscontrol.administrador

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.FirebaseDatabase
import com.miraimx.kinderscontrol.ControlLecturaFirebaseBD
import com.miraimx.kinderscontrol.DatosConsultados
import com.miraimx.kinderscontrol.ListViewGrupo
import com.miraimx.kinderscontrol.databinding.ActivityMainGrupoBinding

class VerGrupos : AppCompatActivity() {

    private lateinit var binding: ActivityMainGrupoBinding
    private lateinit var spGrupo: Spinner
    private lateinit var spGrado: Spinner
    private val listaMiembrosGrupo = mutableListOf<Pair<String?, String?>>()
    private lateinit var adapter: ListViewGrupo
    private lateinit var headerTextView: TextView
    private var aula = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainGrupoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        configSpinners()
        headerTextView = TextView(this)
        configLista()

        binding.btnEditarGrupo.setOnClickListener {
            startActivity(Intent(this, Tutorizacion::class.java).apply {
                putExtra("rol", "Profesor")
                putExtra("grado", spGrado.selectedItem.toString())
                putExtra("grupo", spGrupo.selectedItem.toString())
            })
        }

        binding.imgbtnBuscar.setOnClickListener {
            val aulaSelection = "${spGrado.selectedItem}${spGrupo.selectedItem}"
            if  (aulaSelection != aula){
                aula = aulaSelection
                listaMiembrosGrupo.clear()
                adapter.notifyDataSetChanged()
                busquedaGrupo()
            }
        }
    }

    private fun configLista() {
        val listView = binding.lvMiembrosGrupo
        adapter = ListViewGrupo(this, listaMiembrosGrupo)
        listView.adapter = adapter
        configHeader("Profesor:")
    }

    private fun configHeader(texto: String) {
        binding.lvMiembrosGrupo.removeHeaderView(headerTextView)
        headerTextView = TextView(this)
        headerTextView.text = Html.fromHtml("<b>$texto</b>", 0x12)
        headerTextView.textSize = 20f
        headerTextView.setTextColor(Color.BLACK)
        headerTextView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        binding.lvMiembrosGrupo.addHeaderView(headerTextView, null, false)
    }

    private fun configSpinners() {
        val datosGrupo = arrayOf("A", "B", "C")
        val datosGrado = arrayOf("1", "2", "3")
        spGrado = binding.svGrado
        spGrupo = binding.svGrupo
        adapterSpinner(spGrado, datosGrado)
        adapterSpinner(spGrupo, datosGrupo)
    }

    private fun adapterSpinner(spinner: Spinner, datos: Array<String>) {
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, datos)
        spinner.adapter = arrayAdapter
    }

    private fun busquedaGrupo() {
        val grupoRef = FirebaseDatabase.getInstance().reference.child("grupos/$aula")
        // Consulta para obtener los datos del profesor
        val controlLecturaFirebaseBD = ControlLecturaFirebaseBD(object : DatosConsultados() {
            override fun onDatosConsulta(resultados: MutableList<String>) {
                super.onDatosConsulta(resultados)
                if (resultados.isNotEmpty()) {
                    configHeader("Profesor: ${resultados[1]}")
                }
            }
        })

        controlLecturaFirebaseBD.consultarNodos(grupoRef) { resultado ->
            val texto = if (resultado.isNotEmpty()) resultado[1].second.toString() else ""
            configHeader("Profesor: $texto")
        }

        controlLecturaFirebaseBD.consultarNodos(grupoRef.child("alumnos")) { resultado ->
            if (resultado.isNotEmpty()) {
                resultado.map { listaMiembrosGrupo.add(it) }
            }
            adapter.notifyDataSetChanged()
        }
    }
}