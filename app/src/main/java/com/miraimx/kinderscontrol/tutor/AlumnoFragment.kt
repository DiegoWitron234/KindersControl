package com.miraimx.kinderscontrol.tutor

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.storageMetadata
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
import com.miraimx.kinderscontrol.cuenta.Camara
import com.miraimx.kinderscontrol.databinding.FragmentAlumnoBinding
import java.io.File


class AlumnoFragment : Fragment(), Propiedades {
    private lateinit var binding: FragmentAlumnoBinding
    private val args: AlumnoFragmentArgs by navArgs()
    private lateinit var matricula: String
    private lateinit var lsAccesoAlumnoAdapter: ArrayAdapter<AccesoAlumno>
    val alumnosAccesoLista = mutableListOf<AccesoAlumno>()
    private var fotoAlmacenada: String? = null

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

        cambiarImagen()

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


    @SuppressLint("ClickableViewAccessibility")
    private fun cambiarImagen() {
        val activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val datos = data?.getStringExtra("imagen")
                if (!datos.isNullOrEmpty()) {
                    fotoAlmacenada = datos
                    subirFoto{uri ->
                        val alumnoRef = FirebaseDatabase.getInstance().reference.child("alumnos/$matricula")
                        alumnoRef.updateChildren(hashMapOf<String, Any>("foto_alumno" to uri))
                    }
                }
            }
        }

        binding.btnCambiarImagen.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(200).start()
                }

                MotionEvent.ACTION_UP -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                    val intent = Intent(requireActivity(), Camara::class.java)
                    activityResultLauncher.launch(intent)

                }
            }
            // No consumimos el evento y permitimos que se procese más
            false
        }

    }

    private fun mostrarQR() {
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
                //Log.e("Logaritmo", resultados.size.toString())
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

    private fun subirFoto(onSuccess: (String) -> Unit) {
        if (fotoAlmacenada != null) {
            val storage = Firebase.storage
            val reference = storage.reference
            val imageRef = reference.child("alumnos/$matricula.png")
            val metadata = storageMetadata {
                contentType = imageRef.toString()
            }

            val file = fotoAlmacenada?.let { File(it) }
            val fileUri = Uri.fromFile(file)
            imageRef.putFile(fileUri, metadata).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener {  uri ->
                    val fotoEnlace = uri.toString()
                    if (file != null && file.exists()) {
                        file.delete()

                    }
                    Toast.makeText(requireContext(), "Imagen Subida", Toast.LENGTH_SHORT).show()
                    onSuccess(fotoEnlace)
                }
            }.addOnFailureListener {
                Log.e("Log", "Imagen no subida")
            }
        } else {
            Toast.makeText(requireContext(), "Por favor tome una foto", Toast.LENGTH_SHORT).show()
        }
    }
}