package com.miraimx.kinderscontrol

data class Usuario(
    val id: String,
    val nombre: String,
    var seleccionado: Boolean,
    var identificador: String
)

data class AccesoAlumno(
    val matricula: String,
    val profesor: String,
    val tutor: String,
    val administrador: String,
    val estatus: String,
    val fecha: String,
    val hora: String
)

data class Alumno(
    val matricula: String,
    val nombre: String,
    val apellidos: String,
    val tipoSangre: String,
    val edad: String,
    val grado: String,
    val grupo: String,
    var estatus: String,
    val fecha: String,
    val hora: String,
)
