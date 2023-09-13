package com.miraimx.kinderscontrol

import android.graphics.Color
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val btnAlumnoSeleccion: Button = view.findViewById(R.id.tvItemAlumnos)

    fun render(itemAlumno: Tutorizacion.Usuario, onClickListener: () -> Unit) {
        btnAlumnoSeleccion.setTextColor(Color.BLACK)
        btnAlumnoSeleccion.setBackgroundColor(Color.WHITE)
        btnAlumnoSeleccion.textSize = 20f
        btnAlumnoSeleccion.text = itemAlumno.nombre
        btnAlumnoSeleccion.setOnClickListener {
            if (!itemAlumno.seleccionado) {
                btnAlumnoSeleccion.setTextColor(Color.WHITE)
                btnAlumnoSeleccion.setBackgroundColor(Color.BLUE)
                itemAlumno.seleccionado = true
            } else {
                btnAlumnoSeleccion.setTextColor(Color.BLACK)
                btnAlumnoSeleccion.setBackgroundColor(Color.WHITE)
                itemAlumno.seleccionado = false
            }
            onClickListener()
        }

    }
}
