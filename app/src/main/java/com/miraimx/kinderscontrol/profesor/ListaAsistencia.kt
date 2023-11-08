package com.miraimx.kinderscontrol.profesor

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import com.miraimx.kinderscontrol.R
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.database.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListaAsistencia : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var button: Button
    private val alumnosList = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_asistencia)

        listView = findViewById(R.id.listView)
        button = findViewById(R.id.button)

        database = FirebaseDatabase.getInstance().getReference("alumnos")

        // Obtén la fecha actual
        val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Escucha los cambios en la base de datos
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                alumnosList.clear()
                for (postSnapshot in dataSnapshot.children) {
                    val alumno = postSnapshot.getValue(Alumno::class.java)
                    // Filtra los alumnos que accedieron hoy
                    if (alumno?.accesos?.fecha_acceso == fechaActual) {
                        if (alumno != null) {
                            alumnosList.add("${alumno.nombre_alumno} - ${alumno.accesos.hora_acceso}")
                        }
                    }
                }
                val adapter = ArrayAdapter(this@ListaAsistencia, android.R.layout.simple_list_item_1, alumnosList)
                listView.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Maneja los errores aquí
            }
        })

        button.setOnClickListener {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Asistencia")
            val header = sheet.createRow(0)
            header.createCell(0).setCellValue("Nombre del alumno")
            header.createCell(1).setCellValue("Hora de acceso")

            for (i in alumnosList.indices) {
                val row = sheet.createRow(i + 1)
                val alumno = alumnosList[i].split(" - ")
                row.createCell(0).setCellValue(alumno[0])
                row.createCell(1).setCellValue(alumno[1])
            }

            val values = ContentValues().apply {
                val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                put(MediaStore.MediaColumns.DISPLAY_NAME, "asistencia_$fechaActual.xlsx")
                put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), values)

            uri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    workbook.write(outputStream)
                }
            }
            Toast.makeText(this, "Archivo exportado", Toast.LENGTH_SHORT).show()
        }
    }
}

data class Alumno(
    val accesos: Accesos = Accesos(),
    val nombre_alumno: String = ""
)

data class Accesos(
    val fecha_acceso: String = "",
    val hora_acceso: String = ""
)