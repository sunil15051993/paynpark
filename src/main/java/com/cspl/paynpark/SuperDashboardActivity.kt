package com.cspl.paynpark

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cspl.paynpark.databinding.ActivitySuperDashBinding

class SuperDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySuperDashBinding
    var isToggled = false
    private lateinit var myPref: SharedPreferences
    private lateinit var pdDialog: ProgressDialog
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuperDashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init(){

        binding.imageLogout.setOnClickListener {
            val intent = Intent(this@SuperDashboardActivity, LoginActivity::class.java)
            startActivity(intent)
            finish();
        }

        binding.cardReport.setOnClickListener {
//            binding.iconReport.setImageResource(R.mipmap.ic_report_clr)
            val intent = Intent(this@SuperDashboardActivity, DateWiseReportActivity::class.java)
            startActivity(intent)
        }

        binding.cardUserWiseReport.setOnClickListener {
            val intent = Intent(this@SuperDashboardActivity, UserWiseReportActivity::class.java)
            startActivity(intent)
        }

        binding.cardVehicleWise.setOnClickListener {
            val intent = Intent(this@SuperDashboardActivity, VehicleWiseReportActivity::class.java)
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