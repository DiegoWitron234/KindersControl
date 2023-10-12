package com.miraimx.kinderscontrol

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

interface ModoOscuro {

    fun cancelarModoOscuro(context: Context)
    {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}