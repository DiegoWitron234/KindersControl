package com.miraimx.kinderscontrol

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.miraimx.kinderscontrol.databinding.FragmentAlumnoBinding
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale


class AlumnoFragment : Fragment() {
    private lateinit var binding: FragmentAlumnoBinding
    val args: AlumnoFragmentArgs by navArgs()
    private lateinit var matricula: String
    private lateinit var lsAccesoAlumnoAdapter: ArrayAdapter<AccesoAlumno>
    val alumnosAccesoLista = mutableListOf<AccesoAlumno>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlumnoBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val datosAlumnos = args.datosAlumno
        lsAccesoAlumnoAdapter = ListViewAccesoAdapter(requireContext(), alumnosAccesoLista)
        matricula = datosAlumnos[1]
        binding.imbEditar.setOnClickListener(){
            findNavController().navigate(
                AlumnoFragmentDirections.actionAlumnoFragmentToCamara(
                    matricula = matricula
                )
            )
            //startActivity(Intent(requireContext(), Camara::class.java))
        }

        binding.imbQR.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(200).start()
                }

                MotionEvent.ACTION_UP -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                    val builder = AlertDialog.Builder(requireContext())
                    val vista = layoutInflater.inflate(R.layout.qralumno, null)
                    vista.findViewById<ImageView>(R.id.qrAlumno).setImageBitmap(generateQRCode(matricula))
                    builder.setView(vista)
                    builder.create()
                    builder.show()
                }
            }
            // No consumimos el evento y permitimos que se procese más
            false
        }


        binding.lvRegistrosAcceso.adapter = lsAccesoAlumnoAdapter
        binding.txtAlumnoNombre.text = datosAlumnos[0]
        binding.txtALumnoMatricula.text = "No." + matricula
        binding.txtALumnoEdad.text = "Edad: " + datosAlumnos[2]
        binding.txtAlumnoSangre.text = "T. sangre: " + datosAlumnos[3]
        binding.txtALumnoAula.text = "Grado/Grupo: " + datosAlumnos[4]
        alumnosAccesoLista.clear()
        cargarDatos(matricula)
    }

    private fun cargarDatos(cUserId: String) {
        val database = FirebaseDatabase.getInstance().reference
        val queryCheckin =
            database.child("checkin").orderByChild("matricula")
                .equalTo(cUserId).limitToLast(20)
        queryCheckin.addListenerForSingleValueEvent(object :
            ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(checkinSnapshot: DataSnapshot) {
                var cantidadRegistros = 0
                for (checkin in checkinSnapshot.children) {
                    var estatus = checkin.child("in_out")
                        .getValue(String::class.java)
                    val tiempo = checkin.child("horafecha_check")
                        .getValue(String::class.java)
                    if (estatus != null && tiempo != null) {
                        estatus = if (estatus == "in") {
                            "Ingresó"
                        } else {
                            "Salió"
                        }
                        val accesoAlumno = AccesoAlumno(
                            "",
                            convertirFecha(tiempo),
                            estatus,
                            ""
                        )
                        alumnosAccesoLista.add(accesoAlumno)
                    }
                    cantidadRegistros++
                    if (cantidadRegistros.toLong() == checkinSnapshot.childrenCount) {
                        // Notificar al adaptador sobre los cambios
                        /*var final = alumnosAccesoLista.size-1
                        for (regActual in 0..alumnosAccesoLista.size/2){
                            val regInicial = alumnosAccesoLista[regActual]
                            val regFinal = alumnosAccesoLista[final]
                            alumnosAccesoLista[regActual] = regFinal
                            alumnosAccesoLista[final] = regInicial
                            final--
                        }*/
                        alumnosAccesoLista.reverse()
                        lsAccesoAlumnoAdapter.notifyDataSetChanged()
                        binding.lvRegistrosAcceso.invalidate()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertirFecha(fechaOriginal: String): String {
        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
        val formatoFechaHora =
            DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy|hh:mm a", Locale("es"))

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

    private fun generateQRCode(data: String): Bitmap? {
        return try {
            val bitMatrix = MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 200, 200)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) resources.getColor(R.color.black) else resources.getColor(
                            R.color.white
                        )
                    )
                }
            }
            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }
}