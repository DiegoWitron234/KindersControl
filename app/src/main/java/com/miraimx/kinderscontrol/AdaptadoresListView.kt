package com.miraimx.kinderscontrol

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DividerItemDecoration
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

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
    private val listaAccesos: List<AccesoAlumno>
) :
    ArrayAdapter<AccesoAlumno>(
        context,
        android.R.layout.simple_selectable_list_item,
        listaAccesos
    ), ConvertidorTiempo {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.list_item_accesodatos_layout, null)

        val estatus = listaAccesos[position].estatus
        val tiempo =
            convertirFecha(listaAccesos[position].fecha + " " + listaAccesos[position].hora).split(" - ")
        Log.e("Logaritmo", tiempo.toString())
        val tvEstatus = view.findViewById<TextView>(R.id.tvAccesoDatosEstatus)
        val tvFecha = view.findViewById<TextView>(R.id.tvAccesoFecha)

        tvFecha.text = tiempo[0]+" "+tiempo[1]
        tvEstatus.text = estatus

        return view
    }
}

class ListViewAlumnoAdapter(
    context: Context,
    private val listaUsuario: List<Alumno>
) :
    ArrayAdapter<Alumno>(context, android.R.layout.simple_selectable_list_item, listaUsuario),
    ConvertidorTiempo {
    @SuppressLint("ViewHolder", "InflateParams", "NewApi")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.list_item_alumnos_layout, null)
        val nombre = listaUsuario[position].nombre
        val matricula = listaUsuario[position].matricula
        var estatus = listaUsuario[position].estatus
        val tiempo =
            convertirFecha(listaUsuario[position].fecha + " " + listaUsuario[position].hora)

        val tvNombre = view.findViewById<TextView>(R.id.tvAlumDatosNombre)
        val tvMatricula = view.findViewById<TextView>(R.id.tvAlumDatosMatricula)
        val tvEstatus = view.findViewById<TextView>(R.id.tvAlumDatosEstatus)
        //val tvTiempo = view.findViewById<TextView>(R.id.tvAlumDatosTiempo)

        if (estatus != "Sin registro"){
            estatus = when (estatus){
                "in" -> "Ingresó"
                else -> "Salió"
            } + ": $tiempo"

        }

        tvNombre.text = nombre
        tvMatricula.text = "Matricula: $matricula"
        tvEstatus.text = estatus
        //tvTiempo.text = tiempo

        return view
    }
}

interface ConvertidorTiempo {
    @RequiresApi(Build.VERSION_CODES.O)
    fun convertirFecha(fechaOriginal: String): String {
        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
        val formatoFechaHora =
            DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy - hh:mm a", Locale("es"))
        return try {
            val fechaParseada: Date = formatoEntrada.parse(fechaOriginal) as Date
            val fechaLocal =
                fechaParseada.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            val fechaFormateada: String = formatoFechaHora.format(fechaLocal)
            fechaFormateada
        } catch (e: Exception) {
            println("Error al formatear la fecha: ${e.message}")
            " "
        }
    }
}




