package com.cspl.paynpark

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cspl.paynpark.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    var isToggled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init(){
        binding.cardTicket.setOnClickListener {
            binding.iconTicket.setImageResource(R.mipmap.ic_ticket_clr)
            val intent = Intent(this@DashboardActivity, TicketInActivity::class.java)
            startActivity(intent)
        }
        binding.cardReport.setOnClickListener {
            binding.iconReport.setImageResource(R.mipmap.ic_report_clr)
            val intent = Intent(this@DashboardActivity, TicketInActivity::class.java)
            startActivity(intent)
        }
        binding.cardScan.setOnClickListener {
            binding.iconScan.setImageResource(R.mipmap.ic_scanner_clr)
            val intent = Intent(this, ScannerActivity::class.java)
            startActivity(intent)
        }
        binding.cardInput.setOnClickListener {
            binding.iconInput.setImageResource(R.mipmap.ic_search_clr)
            val intent = Intent(this, FindActivity::class.java)
            startActivity(intent)
        }



    }

    override fun onResume() {
        super.onResume()
        // reset to default image every time user comes back
        binding.iconTicket.setImageResource(R.mipmap.ic_ticket)
        binding.iconReport.setImageResource(R.mipmap.ic_report)
    }

}