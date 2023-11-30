package com.miraimx.kinderscontrol

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.module.AppGlideModule
import com.google.firebase.storage.FirebaseStorage

class ControlFirebaseStg : AppGlideModule() {
    private val storage = FirebaseStorage.getInstance()
    fun cargarImagen(ruta: String, view: ImageView, context: Context) {
        val storageRef = storage.reference.child(ruta)
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(context)
                .asBitmap()  //Funciona como thumbnail
                .load(uri.toString())
                .centerCrop()
                .into(view)
        }.addOnFailureListener {
            // Maneja cualquier error
        }
    }

}
