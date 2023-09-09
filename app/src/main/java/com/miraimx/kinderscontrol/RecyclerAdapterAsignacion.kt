package com.miraimx.kinderscontrol

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapterAsignacion(private val items: MutableList<AsignacionesTutor.Usuario>):
    RecyclerView.Adapter<RecyclerHolderAsignacion>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolderAsignacion {
        val layoutInflater = LayoutInflater.from(parent.context)
        return RecyclerHolderAsignacion(layoutInflater.inflate(R.layout.list_item_asignados, parent, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerHolderAsignacion, position: Int) {
        val elemento = items[position]
        holder.render(elemento)
    }

}


