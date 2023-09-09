package com.miraimx.kinderscontrol

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewAdapter(
    private val dataList: MutableList<Tutorizacion.Usuario>,
    private val onClickListener: () -> Unit, ): RecyclerView.Adapter<RecyclerViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return RecyclerViewHolder(layoutInflater.inflate(R.layout.list_item_alumnos, parent, false))
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val item = dataList[position]
        holder.render(item, onClickListener)
    }
}


