package com.miraimx.kinderscontrol


import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ServicioOyente : Service() {
    private lateinit var uid: String
    //private lateinit var databaseReference: DatabaseReference
    private lateinit var valueEventListener: ValueEventListener
    private lateinit var checkinRef: DatabaseReference
    private lateinit var registrosProcesadosRef: DatabaseReference
    override fun onCreate() {
        super.onCreate()
        uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        //databaseReference = FirebaseDatabase.getInstance().getReference("checkin")

        //Toast.makeText(this, "Servicio en ejecución $uid", Toast.LENGTH_SHORT).show()

        //Crear canal de notificaciones
        createNotificationChannel()

        // Configura el oyente de Firebase
        checkinRef = FirebaseDatabase.getInstance().getReference("checkin")
        registrosProcesadosRef = FirebaseDatabase.getInstance().getReference("registrosProcesados")

        //Agregar el oyente

        valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    val alumnoId = childSnapshot.child("matricula").getValue(String::class.java)

                    // Comprueba si el registro ya ha sido procesado para el alumno
                    val registroProcesadoAlumnoRef = alumnoId?.let { registrosProcesadosRef.child(it) }

                    if(registroProcesadoAlumnoRef != null){
                        registroProcesadoAlumnoRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(registroProcesadoSnapshot: DataSnapshot) {
                                if (!registroProcesadoSnapshot.hasChild(childSnapshot.key!!)) {
                                    // El registro no ha sido procesado para este alumno

                                    // Busca los tutores relacionados al alumno en "tutorizacion"
                                    val tutorizacionRef = FirebaseDatabase.getInstance().getReference("tutorizacion")
                                    tutorizacionRef.orderByChild("matricula").equalTo(alumnoId)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(tutorizacionSnapshot: DataSnapshot) {
                                                for (tutorizacionChildSnapshot in tutorizacionSnapshot.children) {
                                                    val tutorId = tutorizacionChildSnapshot.child("tutor_id").getValue(String::class.java)

                                                    if (tutorId == uid) {
                                                        // Muestra un Toast
                                                        Toast.makeText(applicationContext, "Se agregó un registro de chekin", Toast.LENGTH_SHORT).show()

                                                        val horafecha = childSnapshot.child("horafecha_check").getValue(String::class.java)
                                                        val inOut = childSnapshot.child("in_out").getValue(String::class.java)
                                                        //val matricula = childSnapshot.child("matricula").getValue(String::class.java)

                                                        // Obtén el nombre del alumno
                                                        obtenerNombreAlumno(alumnoId) { nombreAlumno ->
                                                            // Muestra una notificación con el nombre del alumno
                                                            showNotification(horafecha, inOut, nombreAlumno)
                                                        }

                                                        // Muestra una notificación
                                                        //showNotification(horafecha, inOut, alumnoId)

                                                        // Agrega el ID del registro a "registrosProcesados" para este tutor
                                                        registroProcesadoAlumnoRef.child(childSnapshot.key!!).setValue(true)
                                                    }
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                Log.w("Registro", "Error al leer la base de datos: $error", error.toException())
                                            }
                                        })
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.w("Registro", "Error al leer la base de datos: $error", error.toException())
                            }
                        })
                    }else{
                        Toast.makeText(
                            this@ServicioOyente,
                            "No hay registros procesados",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Registro", "Error al leer la base de datos: $error", error.toException())
            }
        }

        // Agrega el oyente a la referencia de la base de datos

        checkinRef.addValueEventListener(valueEventListener)

        // Muestra una notificación persistente
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Elimina el oyente cuando el servicio se detiene
        checkinRef.removeEventListener(valueEventListener)
        registrosProcesadosRef.removeEventListener(valueEventListener)
    }

    private fun showNotification(horafecha: String?, inOut: String?, nombre: String?) {
        // Crea un intent para abrir la actividad "AsignacionesTutor"
        val intent = Intent(this, AsignacionesTutor::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(
            this,
            2345,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Construye y muestra una notificación
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Nuevo registro de check-in")
            .setContentText("In/Out: $inOut, Hora/Fecha: $horafecha\n, Alumno: $nombre")
            .setSmallIcon(R.drawable.notification)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Configura el PendingIntent aquí

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun createNotification(): Notification {
        // Crea una notificación persistente (en primer plano)
        val notificationIntent = Intent(this, PanelUsuario::class.java) // Actividad principal de la aplicación
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Servicio en ejecución")
            .setContentText("El servicio está funcionando en segundo plano")
            .setSmallIcon(R.drawable.notification)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notificaciones de checkin",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    // Función para obtener el nombre del alumno a partir de su matrícula
    private fun obtenerNombreAlumno(matricula: String?, callback: (String) -> Unit) {
        if (matricula != null) {
            val alumnosRef = FirebaseDatabase.getInstance().getReference("alumnos")
            alumnosRef.child(matricula).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val nombreAlumno = dataSnapshot.child("nombre_alumno").getValue(String::class.java) ?: ""
                    callback(nombreAlumno)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("Registro", "Error al leer el nombre del alumno: $error", error.toException())
                    callback("") // Devuelve un nombre vacío en caso de error
                }
            })
        } else {
            callback("") // Devuelve un nombre vacío si la matrícula es nula
        }
    }


    companion object {
        private const val CHANNEL_ID = "MyChannel"
        private const val NOTIFICATION_ID = 1
    }
}