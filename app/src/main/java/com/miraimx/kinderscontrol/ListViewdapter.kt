package com.miraimx.kinderscontrol

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class ListViewUsuarioAdapter(
    context: Context,
    private val listaUsuario: List<Usuario>
) :
    ArrayAdapter<Usuario>(context, android.R.layout.simple_selectable_list_item, listaUsuario) {
    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val nombreAlumno = listaUsuario[position].nombre
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(android.R.layout.simple_list_item_1, null)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = nombreAlumno
        return view
    }

}

class ListViewAccesoAdapter(
    context: Context,
    private val listaUsuario: List<AccesoAlumno>
) :
    ArrayAdapter<AccesoAlumno>(context, android.R.layout.simple_selectable_list_item, listaUsuario) {
    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.list_item_accesodatos_layout, null)

        val nombre = listaUsuario[position].nombre
        val estatus = listaUsuario[position].estatus
        val tiempo = listaUsuario[position].tiempo

        val tvNombre = view.findViewById<TextView>(R.id.tvAccesoDatosNombre)
        val tvEstatus = view.findViewById<TextView>(R.id.tvAccesoDatosEstatus)
        val tvTiempo = view.findViewById<TextView>(R.id.tvAccesoDatosTiempo)

        tvNombre.text = nombre
        tvTiempo.text = tiempo
        if(estatus == "in"){
            tvEstatus.text = "Ingresó"
        }else{
            tvEstatus.text = "Salió"
        }
        return view
    }


}
