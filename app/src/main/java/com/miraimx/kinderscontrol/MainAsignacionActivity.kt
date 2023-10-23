package com.miraimx.kinderscontrol

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.miraimx.kinderscontrol.databinding.ActivityMainAsignacionBinding

class MainAsignacionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainAsignacionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainAsignacionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
        /*supportFragmentManager.commit {
            setReorderingAllowed(true)
        }*/
    }
}