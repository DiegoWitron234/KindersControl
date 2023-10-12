package com.miraimx.kinderscontrol

data class Usuario(
    val id: String,
    val nombre: String,
    var seleccionado: Boolean,
    var usuario: String
)

data class AccesoAlumno(
    val nombre: String,
    val tiempo: String,
    val estatus: String,
    var matricula: String,
)

data class Alumno(
    val nombre: String,
    var matricula: String
)
