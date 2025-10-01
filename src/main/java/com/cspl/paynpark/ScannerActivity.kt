package com.cspl.paynpark

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.google.zxing.integration.android.IntentIntegrator

class ScannerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start scanner
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Scan a QR Code")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(false) // important
        integrator.captureActivity = PortraitCaptureActivity::class.java
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                // QR Code scanned successfully
                val scannedData = result.contents
                val lines: List<String> = scannedData.split("\n")
                var recpNo = ""
                var date = ""
                var vehNo = ""
                var vehType = ""
                var inTime = ""
                var paid = ""

                for (line in lines) {
                    when {
                        line.startsWith("Receipt No:") -> recpNo = line.removePrefix("Receipt No:").trim()
                        line.startsWith("Date:") -> date = line.removePrefix("Date:").trim()
                        line.startsWith("Vehicle No:") -> vehNo = line.removePrefix("Vehicle No:").trim()
                        line.startsWith("Vehicle Type:") -> vehType = line.removePrefix("Vehicle Type:").trim()
                        line.startsWith("In Time:") -> inTime = line.removePrefix("In Time:").trim()
                        line.startsWith("Paid:") -> paid = line.removePrefix("Paid:").trim()
                    }
                }
                Log.e("SCANNER", "onActivityResult: "+ paid)

                val intent = Intent(this, TicketOutActivity::class.java).apply {
                    putExtra("recpNo", recpNo)
                    putExtra("date", date)
                    putExtra("vehNo", vehNo)
                    putExtra("vehType", vehType)
                    putExtra("inTime", inTime)
                    putExtra("paid", paid.toInt())
                }
                startActivity(intent)
                finish()
            } else {
                // Scan cancelled
                finish()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}