package com.miraimx.kinderscontrol

data class Usuario(
    val id: String,
    val nombre: String,
    var seleccionado: Boolean,
    val usuario: String
)

data class AccesoAlumno(
    val nombre: String,
    val tiempo: String,
    val estatus: String,
    val matricula: String,
)

data class Alumno(
    val nombre: String,
    val matricula: String,
    val aula: String,
    val edad: String,
    val tipoSangre: String,
    var estatus: String,
    var tiempo: String
)
