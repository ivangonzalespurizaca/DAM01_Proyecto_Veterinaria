package com.ivandev.proyectoveterinaria.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ivandev.proyectoveterinaria.databinding.ActivityRegistroBinding

class RegistroActivity : AppCompatActivity() {
    private lateinit var binding : ActivityRegistroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}