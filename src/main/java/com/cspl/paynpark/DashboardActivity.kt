package com.cspl.paynpark

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.cspl.paynpark.api.Api
import com.cspl.paynpark.databinding.ActivityDashboardBinding
import com.cspl.paynpark.dbhelper.AppDatabase
import com.cspl.paynpark.model.HeaderFooter
import com.cspl.paynpark.model.VehicFare
import org.json.JSONObject
import java.util.concurrent.Executors

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    var isToggled = false
    private lateinit var myPref: SharedPreferences
    private lateinit var pdDialog: ProgressDialog
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myPref = getSharedPreferences("paynpark", MODE_PRIVATE)
        val call: Boolean = myPref.getBoolean("api_vh_fare", false)

        if (!call) {
            callGetPrice()
        }

        init()
    }

    private fun init(){
        binding.imageRefresh.setOnClickListener { callGetPrice() }
        binding.imageLogout.setOnClickListener {
            val intent = Intent(this@DashboardActivity, MainActivity::class.java)
            startActivity(intent)
            finish();
        }
        binding.cardTicket.setOnClickListener {
            val intent = Intent(this@DashboardActivity, TicketInActivity::class.java)
            startActivity(intent)
        }
        binding.cardReport.setOnClickListener {
//            binding.iconReport.setImageResource(R.mipmap.ic_report_clr)
            val intent = Intent(this@DashboardActivity, ReportActivity::class.java)
            startActivity(intent)
        }
        binding.cardScan.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            startActivity(intent)
        }
        binding.cardInput.setOnClickListener {
            val intent = Intent(this, FindActivity::class.java)
            startActivity(intent)
        }

    }

    private fun callGetPrice() {
        pdDialog = ProgressDialog(this@DashboardActivity)
        pdDialog.setTitle("Please wait...")
        pdDialog.setCancelable(false)

        val url = Api.VEHICLE_FARE
        Log.e("res_type", url)
        pdDialog.show()

        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String?> { response ->
                pdDialog.dismiss()
                try {
                    Log.e("res_type", response!!)
                    Log.e("res_type", url)

                    val rootObj = JSONObject(response)

                    val faresMatrix = rootObj.getJSONObject("fares_matrix")

                    val db = AppDatabase.getInstance(this@DashboardActivity)
                    val executor = Executors.newSingleThreadExecutor()

                    // Loop through vehicle types (keys)
                    val keys = faresMatrix.keys()
                    while (keys.hasNext()) {
                        val vehicleType = keys.next()
                        val faresArray = faresMatrix.getJSONArray(vehicleType)

                        for (i in 0 until faresArray.length()) {
                            val fareObj = faresArray.getJSONObject(i)

                            val hours = fareObj.getInt("hours")
                            val price = fareObj.getInt("price")

                            val fare = VehicFare(vehicleType, hours.toLong(), price)
                            executor.execute { db.fareDao().insert(fare) }
                            val editor = myPref.edit()
                            editor.putBoolean("api_vh_fare", true)
                            editor.apply()
                        }

                        callGetHeaderFooter()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("category_error", "Error: " + e.message)
                }
            },
            Response.ErrorListener { error ->
                pdDialog.dismiss()
                Log.e("RequestError", "Registration Error: $error")
                Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["username"] = "abcd"
                return params
            }
        }

        val requestQueue = Volley.newRequestQueue(this@DashboardActivity)
        requestQueue.add(stringRequest)
    }

    private fun callGetHeaderFooter() {
        val url = Api.HEADER_FOOTER
        Log.e("header_api", url)

        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String?> { response ->
                try {
                    Log.e("header_res", response!!)
                    val rootObj = JSONObject(response)

                    if (rootObj.getBoolean("status")) {
                        val dataObj = rootObj.getJSONObject("data")

                        val headerObj = dataObj.getJSONObject("header")
                        val header1 = headerObj.optString("header1", "Pay & Park")
                        val header2 = headerObj.optString("header2", "")
                        val header3 = headerObj.optString("header3", "")
                        val header4 = headerObj.optString("header4", "")

                        val footerObj = dataObj.getJSONObject("footer")
                        val footer1 = footerObj.optString("footer1", "Thank You visit again")
                        val footer2 = footerObj.optString("footer2", "")
                        val footer3 = footerObj.optString("footer3", "")
                        val footer4 = footerObj.optString("footer4", "")

                        //Now save into DB (Room)
                        val db = AppDatabase.getInstance(this@DashboardActivity)
                        Executors.newSingleThreadExecutor().execute {
                            val hf = HeaderFooter(
                                header1, header2, header3, header4,
                                footer1, footer2, footer3, footer4
                            )
                            db.headerFooterDao().insert(hf)
                        }
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("header_error", "Error: " + e.message)
                }
            },
            Response.ErrorListener { error ->
                Log.e("header_request", "Error: $error")
                Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params["username"] = "abcd"
                return params
            }
        }

        Volley.newRequestQueue(this@DashboardActivity).add(stringRequest)

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