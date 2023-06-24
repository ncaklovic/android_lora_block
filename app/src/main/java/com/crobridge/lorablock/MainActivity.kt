package com.crobridge.lorablock

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import com.crobridge.lorablock.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.appToolbar.setTitle(R.string.app_name)
        setSupportActionBar(binding.appToolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.overflow_menu, menu)
        return true
    }

}