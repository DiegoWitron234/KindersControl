package com.miraimx.kinderscontrol

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView

class ListAdapter{

    fun <T>Adapter(context: Context, lista: MutableList<T>, view: ListView): ArrayAdapter<T>{
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, lista)
        view.adapter = adapter
        return adapter
    }
}

class TutorizacionAdapter(
    context: Context,
    private val listaUsuario: List<Usuario>
) :
    ArrayAdapter<Usuario>(context, android.R.layout.simple_selectable_list_item, listaUsuario) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val nombreAlumno = listaUsuario[position].nombre
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(android.R.layout.simple_list_item_1, null)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = nombreAlumno
        return view
    }

}