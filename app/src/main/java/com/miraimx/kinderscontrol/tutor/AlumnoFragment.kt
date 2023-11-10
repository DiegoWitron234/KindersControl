package com.miraimx.kinderscontrol.tutor

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.miraimx.kinderscontrol.AccesoAlumno
import com.miraimx.kinderscontrol.ControlLecturaFirebaseBD
import com.miraimx.kinderscontrol.ControlFirebaseStg
import com.miraimx.kinderscontrol.DatosConsultados
import com.miraimx.kinderscontrol.ListViewAccesoAdapter
import com.miraimx.kinderscontrol.Propiedades
import com.miraimx.kinderscontrol.R
import com.miraimx.kinderscontrol.databinding.FragmentAlumnoBinding


class AlumnoFragment : Fragment(), Propiedades {
    private lateinit var binding: FragmentAlumnoBinding
    private val args: AlumnoFragmentArgs by navArgs()
    private lateinit var matricula: String
    private lateinit var lsAccesoAlumnoAdapter: ArrayAdapter<AccesoAlumno>
    val alumnosAccesoLista = mutableListOf<AccesoAlumno>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlumnoBinding.inflate(inflater, container, false)
        cancelarModoOscuro(requireActivity())
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val controlFirebaseStg = ControlFirebaseStg()
        val datosAlumnos = args.datosAlumno
        lsAccesoAlumnoAdapter = ListViewAccesoAdapter(requireContext(), alumnosAccesoLista)
        matricula = datosAlumnos[1]

        binding.imbQR.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(200).start()
                }

                MotionEvent.ACTION_UP -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                    mostrarQR()
                }
            }
            // No consumimos el evento y permitimos que se procese más
            false
        }

        binding.lvRegistrosAcceso.adapter = lsAccesoAlumnoAdapter
        binding.txtAlumnoNombre.text = datosAlumnos[0]
        binding.txtALumnoMatricula.text = "Matricula: $matricula"
        binding.txtALumnoEdad.text = "Edad: ${datosAlumnos[2]}"
        binding.txtAlumnoSangre.text = "T. Sangre: ${datosAlumnos[3]}"
        binding.txtALumnoAula.text = "Grupo: ${datosAlumnos[4]}"
        controlFirebaseStg.cargarImagen(
            "alumnos/$matricula.png",
            binding.imgAlumno,
            requireActivity()
        )

        alumnosAccesoLista.clear()
        cargarDatos(matricula)
    }

    private fun mostrarQR(){
        val builder = AlertDialog.Builder(requireContext())
        val vista = layoutInflater.inflate(R.layout.qralumno, null)
        vista.findViewById<ImageView>(R.id.qrAlumno)
            .setImageBitmap(generateQRCode(matricula))
        builder.setView(vista)
        builder.create()
        builder.show()
    }

    private fun cargarDatos(cUserId: String) {

        val controlLecturaFirebaseBD = ControlLecturaFirebaseBD(object : DatosConsultados() {
            override fun onDatosAcceso(resultados: MutableList<AccesoAlumno>) {
                super.onDatosAcceso(resultados)
                Log.e("Logaritmo", resultados.size.toString())
                alumnosAccesoLista.reverse()
                lsAccesoAlumnoAdapter.notifyDataSetChanged()
                binding.lvRegistrosAcceso.invalidate()
            }
        })
        controlLecturaFirebaseBD.consultaAccesos(cUserId, alumnosAccesoLista)
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