package com.miraimx.kinderscontrol

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerHolderAsignacion(view: View): RecyclerView.ViewHolder(view) {
    private val textViewRegistro: TextView = view.findViewById(R.id.textViewItem)

    fun render(item: AsignacionesTutor.Usuario){
        textViewRegistro.text = item.nombre
    }
}