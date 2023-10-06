package com.miraimx.kinderscontrol

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

interface ModoOscuro {

    fun cancelarModoOscuro(context: Context){
        when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES ->         /* si está activo el modo oscuro lo desactiva */
                AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }
}