package com.miraimx.kinderscontrol

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue


class ControlFirebaseBD(private val callback: DatosConsultados) {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun consultaTutorizacion(
        tabla: String,
        nombre: String,
        atributoId: String,
        atributoBuscar: String,
        lista: MutableList<Usuario>,
        orden: Boolean
    ) {
        if (nombre.isNotBlank()) {
            val database = database.child(tabla)
            val alumnosQuery =
                database.orderByChild(atributoBuscar).startAt(nombre).endAt(nombre + "\uf8ff")
            alumnosQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista.clear() // Borra la lista antes de agregar nuevos resultados
                    snapshot.children.mapNotNull { usuario ->
                        val id = usuario.child(atributoId).getValue(String::class.java)
                        val nombreUsuario =
                            usuario.child(atributoBuscar).getValue(String::class.java)
                        if (!nombreUsuario.isNullOrEmpty() && !id.isNullOrEmpty()) {
                            Usuario(
                                if (orden) nombreUsuario else id,
                                if (orden) id else nombreUsuario,
                                false,
                            )
                        } else null
                    }.also { usuarios ->
                        lista.addAll(usuarios)
                        callback.onDatosUsuario(lista)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    fun consultaTutorizaciones(claveUsuario: String, nombreUsuario: String, lista: MutableList<Usuario>) {
        lista.clear()
        val query = database.child("alumnos").orderByChild("tutores/$claveUsuario").equalTo(nombreUsuario)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.mapNotNull { registro ->
                    val matricula = registro.child("matricula").getValue<String>()
                    val nombreAlumno = registro.child("nombre_alumno").getValue<String>()
                    //Log.e("LOG", matricula + nombreAlumno)
                    if (!matricula.isNullOrEmpty() && !nombreAlumno.isNullOrEmpty()) {
                        Usuario(matricula, nombreAlumno, false)
                    } else null
                }.also { registro ->
                    lista.addAll(registro)
                    callback.onDatosUsuario(lista)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun consultar(consulta: Query, listaAtributos: Array<String>) {
        val listaResultados: MutableList<String> = mutableListOf()
        consulta.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.mapNotNull { registro ->
                    listaAtributos.mapNotNull { atributo ->
                        val dato = registro.child(atributo).getValue<String>()
                        if (!dato.isNullOrEmpty()) {
                            dato
                        } else null
                    }.also { atributos ->
                        listaResultados.addAll(atributos)
                    }
                }.also {
                    callback.onDatosConsulta(listaResultados)
                }
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    fun consultaAsignacion(
        query: Query,
        listaAlumnos: MutableList<Alumno>
    ) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.mapNotNull { alumno ->
                    val matricula = alumno.child("matricula").getValue<String>()
                    val nombre = alumno.child("nombre_alumno").getValue<String>()
                    val tiposangre = alumno.child("tiposangre_alumno").getValue<String>()
                    val edad = alumno.child("edad_alumno").getValue<String>()
                    val grado = alumno.child("grado").getValue<String>() ?: ""
                    val grupo = alumno.child("grupo").getValue<String>() ?: ""
                    val estatus =
                        alumno.child("accesos/estatus").getValue<String>() ?: "Sin registro"
                    val fecha = alumno.child("accesos/fecha_acceso").getValue<String>() ?: ""
                    val hora = alumno.child("accesos/hora_acceso").getValue<String>() ?: ""

                    if (listOf(
                            matricula,
                            nombre,
                            tiposangre,
                            edad
                        ).all { !it.isNullOrEmpty() }
                    ) {
                        Alumno(
                            matricula!!,
                            nombre!!,
                            tiposangre!!,
                            edad!!,
                            grado,
                            grupo,
                            estatus,
                            fecha,
                            hora,
                            ""
                        )
                    } else null
                }.also { alumnos ->
                    listaAlumnos.addAll(alumnos)
                    callback.onDatosAlumno(listaAlumnos)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun consultaAccesos(matricula: String, listaAccesos: MutableList<AccesoAlumno>) {
        //cambiar a acceso
        database.child("accesos").orderByChild("matricula").equalTo(matricula).limitToLast(20)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.mapNotNull { rAcceso ->
                        val profesor = rAcceso.child("profesor_id").getValue<String>()
                        val tutor = rAcceso.child("tutor_id").getValue<String>() ?: ""
                        val administrador = rAcceso.child("admin_id").getValue<String>() ?: ""
                        var estatus = rAcceso.child("estatus").getValue<String>()
                        val fecha = rAcceso.child("fecha_acceso").getValue<String>()
                        val hora = rAcceso.child("hora_acceso").getValue<String>()
                        if (listOf(profesor, estatus, fecha, hora).all { !it.isNullOrEmpty() }) {
                            estatus = if (estatus == "in") "Ingresó" else "Salió"
                            AccesoAlumno(
                                matricula, profesor!!, tutor, administrador,
                                estatus, fecha!!, hora!!
                            )
                        } else null
                    }.also { registros ->
                        listaAccesos.addAll(registros)
                        callback.onDatosAcceso(listaAccesos)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun fbConsulta(query: Query, listaAtr: MutableList<HashMap<String, Any>>) {
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.mapNotNull { documento ->
                    val registro = documento.getValue<HashMap<String, Any>>()
                    if (!registro.isNullOrEmpty()) {
                        listaAtr.add(registro)
                    }
                }.also {
                    callback.onDatos(listaAtr)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}