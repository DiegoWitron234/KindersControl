package com.miraimx.kinderscontrol.profesor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
import com.miraimx.kinderscontrol.profesor.ui.theme.KindersControlTheme
//import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
//import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListaAsistencia : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KindersControlTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ListaDeAsistencia()
                }
            }
        }
    }
}

@Composable
fun ListaDeAsistencia() {
    var alumnos by remember { mutableStateOf(listOf<Alumno>()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            // Obtén la fecha actual
            val fechaActual = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            // Crea una referencia a la base de datos
            val database = FirebaseDatabase.getInstance().getReference("alumnos")

            // Escucha los cambios en la base de datos
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val lista = mutableListOf<Alumno>()
                    for (postSnapshot in dataSnapshot.children) {
                        val alumno = postSnapshot.getValue(Alumno::class.java)
                        // Filtra los alumnos que accedieron hoy
                        if (alumno?.accesos?.fecha_acceso == fechaActual) {
                            if (alumno != null) {
                                lista.add(alumno)
                            }
                        }
                    }
                    alumnos = lista
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Maneja los errores aquí
                }
            })
        }) {
            Text("Lista de asistencia")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Muestra la lista de alumnos
        for (alumno in alumnos) {
            Text(text = "${alumno.nombre_alumno} - ${alumno.accesos.hora_acceso}")
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
