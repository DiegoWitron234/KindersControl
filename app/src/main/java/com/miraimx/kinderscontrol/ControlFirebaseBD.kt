package com.miraimx.kinderscontrol

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class ControlFirebaseBD(private val consultaListener: ConsultaListener)  {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val referencia: DatabaseReference =database.reference

    fun consultaEspecifica(tablas: Array<String>, children: Array<String>, atributos:Array<String>, id: String){
        val resultados = mutableListOf<String>()
        for (tabla in tablas) {
            val refTabla = referencia.child(tabla)
            for (child in children) {
                val query = refTabla.orderByChild(child).equalTo(id)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (entidad in snapshot.children) {
                            for (atributo in atributos) {
                                val valor = entidad.child(atributo).getValue(String::class.java)
                                if (valor != null) {
                                    resultados.add(valor)
                                    //Log.d("TAG", "Valor de $atributo: $valor")
                                }

                            }
                        }
                        consultaListener.onDataListo(resultados)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Manejar el error, si es necesario
                        //Log.e("TAG", "Error en la consulta: ${error.message}")
                    }
                })
            }
        }
    }

    fun consulta(peticion: Query, datosRecabar: Array<String>){
        val resultados = mutableListOf<String>()

        peticion.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (entidad in snapshot.children) {
                    for (dato in datosRecabar){
                        val datoEntidad = entidad.child(dato).getValue(String::class.java)
                        if (datoEntidad != null) {
                            resultados.add(datoEntidad)
                        }
                    }
                    consultaListener.onDataListo(resultados)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

}