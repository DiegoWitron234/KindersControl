package com.miraimx.kinderscontrol

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue


class ControlLecturaFirebaseBD(private val callback: DatosConsultados) {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun consultaTutorizacion(
        tabla: String,
        nombre: String,
        rol: String,
        atributoId: Array<String>,
        atributoBuscar: Array<String>,
        lista: MutableList<Usuario>,
        orden: Boolean
    ) {
        if (nombre.isNotBlank()) {
            val database = database.child(tabla)
            consulta(database, tabla, nombre, atributoId, atributoBuscar, 0, lista, orden, rol)
        }
    }

    private fun consulta(
        database: DatabaseReference,
        tabla: String,
        nombre: String,
        atributoId: Array<String>,
        atributoBuscar: Array<String>,
        index: Int,
        lista: MutableList<Usuario>,
        orden: Boolean,
        rol: String
    ) {
        val query =
            database.orderByChild(atributoBuscar[index]).startAt(nombre).endAt(nombre + "\uf8ff")
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //lista.clear() // Borra la lista antes de agregar nuevos resultados
                if (snapshot.exists()) {
                    snapshot.children.mapNotNull { usuario ->
                        val id = usuario.child(atributoId[0]).getValue(String::class.java)
                        val nombreUsuario =
                            usuario.child(atributoBuscar[index]).getValue(String::class.java)
                        val apellidosUsuario =
                            usuario.child(atributoBuscar[1 - index]).getValue<String>()
                        datosUsuario(tabla, usuario, nombreUsuario, apellidosUsuario, id, orden, rol)
                    }.also { usuarios ->
                        lista.addAll(usuarios)
                        callback.onDatosUsuario(lista)
                    }
                } else if (index == 0 && atributoBuscar[1].isNotEmpty()) {
                    // Si no se encontraron resultados con atributoBuscar[0], intenta con atributoBuscar[1]
                    consulta(database, tabla, nombre, atributoId, atributoBuscar, 1, lista, orden, rol)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun datosUsuario(
        tabla: String,
        usuario: DataSnapshot,
        nombreUsuario: String?,
        apellidosUsuario: String?,
        id: String?,
        orden: Boolean,
        rol: String
    ): Usuario? {
        if (!nombreUsuario.isNullOrEmpty() && !id.isNullOrEmpty() && !apellidosUsuario.isNullOrEmpty()) {
            if (tabla == "usuarios") {
                val tipoUsuario = usuario.child("rol").getValue<String>()
                if (!tipoUsuario.isNullOrEmpty() && tipoUsuario == rol) {
                    return Usuario(
                        if (orden) "$nombreUsuario $apellidosUsuario" else id,
                        if (orden) id else "$nombreUsuario $apellidosUsuario",
                        false, ""
                    )
                }
            } else {
                return Usuario(
                    if (orden) "$nombreUsuario $apellidosUsuario" else id,
                    if (orden) id else "$nombreUsuario $apellidosUsuario",
                    false, ""
                )
            }
        }
        return null
    }

    fun consultaTutorizaciones(
        claveUsuario: String,
        nombreUsuario: String,
        lista: MutableList<Usuario>
    ) {
        lista.clear()
        val query =
            database.child("alumnos").orderByChild("tutores/$claveUsuario").equalTo(claveUsuario)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.mapNotNull { registro ->
                    val matricula = registro.child("matricula").getValue<String>()
                    val nombreAlumno = registro.child("nombre_alumno").getValue<String>()
                    val apellidosAlumno = registro.child("apellidos_alumno").getValue<String>()
                    val nombre = "$nombreAlumno $apellidosAlumno"
                    //Log.e("LOG", matricula + nombreAlumno)
                    if (!matricula.isNullOrEmpty() && !nombreAlumno.isNullOrEmpty()) {
                        Usuario(matricula, nombre, false, "")
                    } else null
                }.also { registro ->
                    lista.addAll(registro)
                    callback.onDatosUsuario(lista)
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
                    val apellidos = alumno.child("apellidos_alumno").getValue<String>() ?: ""
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
                            apellidos,
                            tiposangre!!,
                            edad!!,
                            grado,
                            grupo,
                            estatus,
                            fecha,
                            hora,
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


    fun consultar(consulta: Query, listaAtributos: Array<String>) {
        val listaResultados: MutableList<String> = mutableListOf()
        consulta.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.mapNotNull { registro ->
                    listaAtributos.map { atributo ->
                        val dato = registro.child(atributo).getValue<String>()
                        if (!dato.isNullOrEmpty()) dato else ""
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
    fun consultarNodos(
        query: Query,
        callback: (MutableList<Pair<String?, String?>>) -> Unit
    ) {
        val datos = mutableListOf<Pair<String?, String?>>()
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.map { registro ->
                    val clave = registro.key
                    if (!registro.hasChildren()){
                        val valor = registro.getValue<String>()
                        if (!valor.isNullOrEmpty() || !clave.isNullOrEmpty()) {
                            datos.add(Pair(clave, valor))
                        }
                    }
                }.also {
                    callback(datos)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}