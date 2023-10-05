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

        Toast.makeText(this, "Servicio en ejecución $uid", Toast.LENGTH_SHORT).show()

        //Crear canal de notificaciones
        createNotificationChannel()

        // Configura el oyente de Firebase
        checkinRef = FirebaseDatabase.getInstance().getReference("checkin")
        registrosProcesadosRef = FirebaseDatabase.getInstance().getReference("registrosProcesados")

        //Agregar el oyente

        checkinRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    val alumnoId = childSnapshot.child("matricula").getValue(String::class.java)

                    // Comprueba si el registro ya ha sido procesado para el alumno
                    val registroProcesadoAlumnoRef = alumnoId?.let { registrosProcesadosRef.child(it) }

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
                                                    Toast.makeText(applicationContext, "Se agregó un registro para el alumno $alumnoId", Toast.LENGTH_SHORT).show()

                                                    val empleadoId = childSnapshot.child("empleado_id").getValue(String::class.java)
                                                    val horafecha = childSnapshot.child("horafecha_check").getValue(String::class.java)
                                                    val inOut = childSnapshot.child("in_out").getValue(String::class.java)
                                                    val matricula = childSnapshot.child("matricula").getValue(String::class.java)

                                                    // Muestra una notificación
                                                    showNotification(empleadoId, horafecha, inOut, matricula)

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
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Registro", "Error al leer la base de datos: $error", error.toException())
            }
        })

        // Agrega el oyente a la referencia de la base de datos

        //checkinRef.addValueEventListener(valueEventListener)

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

    private fun showNotification(empleadoId: String?, horafecha: String?, inOut: String?, matricula: String?) {
        // Construye y muestra una notificación
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Nuevo registro de check-in")
            .setContentText("In/Out: $inOut, Hora/Fecha: $horafecha\n, Matrícula: $matricula")
            .setSmallIcon(R.drawable.notification)
            .setAutoCancel(true)

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


    companion object {
        private const val CHANNEL_ID = "MyChannel"
        private const val NOTIFICATION_ID = 1
    }
}