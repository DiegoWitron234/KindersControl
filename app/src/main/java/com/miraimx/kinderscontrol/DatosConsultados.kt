package com.miraimx.kinderscontrol

abstract class DatosConsultados {
    open fun onDatosUsuario(resultados: MutableList<Usuario>) {}

    open fun onDatosConsulta(resultados: MutableList<String>) {}

    open fun onDatosAlumno(resultados: MutableList<Alumno>) {}

    open fun onDatosAcceso(resultados: MutableList<AccesoAlumno>) {}

    open fun onDatos(resultados: MutableList<HashMap<String, Any>>) {}
}