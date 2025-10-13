package com.cspl.paynpark

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cspl.paynpark.databinding.ActivityAdminDashBinding

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashBinding
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init(){

        binding.imageLogout.setOnClickListener {
            val intent = Intent(this@AdminDashboardActivity, LoginActivity::class.java)
            startActivity(intent)
            finish();
        }
        binding.cardSetting.setOnClickListener {
            val intent = Intent(this@AdminDashboardActivity, SettingsActivity::class.java)
            startActivity(intent)
        }

    }
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()

        Handler().postDelayed({
            doubleBackToExitPressedOnce = false
        }, 5000) // Reset the flag after 2 seconds
    }

}