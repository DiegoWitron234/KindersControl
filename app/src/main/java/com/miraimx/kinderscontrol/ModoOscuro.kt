package com.miraimx.kinderscontrol

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

interface ModoOscuro {

    fun cancelarModoOscuro(context: Context) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

}