package com.cspl.paynpark

import android.R
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.cspl.paynpark.databinding.ActivityUserReportBinding
import com.cspl.paynpark.dbhelper.AppDatabase
import com.cspl.paynpark.model.LoginMaster
import com.cspl.paynpark.model.VehicType
import java.util.Calendar

class UserWiseReportActivity : AppCompatActivity() {
    private var db: AppDatabase? = null
    private var binding: ActivityUserReportBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserReportBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())

        db = AppDatabase.getInstance(this@UserWiseReportActivity)

        initCall()
    }

    private fun initCall(){
        binding!!.imageBack.setOnClickListener {
            onBackPressed()
        }

        db!!.loginDao().allUser.observe(
            this
        ) { userList: List<LoginMaster> ->
            val vehicleTypes: MutableList<String> =
                ArrayList()
            for (t in userList) {
                vehicleTypes.add(t.userId)
            }

            val adapterVehicle = ArrayAdapter<String>(
                this@UserWiseReportActivity,
                R.layout.simple_dropdown_item_1line,
                vehicleTypes
            )
            binding!!.spinnerOperator.setAdapter(adapterVehicle)
        }

        binding?.edittextSelectDate?.setOnClickListener(View.OnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format selected date (dd-MM-yyyy)
                    val selectedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
                    binding?.edittextSelectDate?.setText(selectedDate)
                },
                year,
                month,
                day
            )

            datePicker.show()
        })

        binding?.buttonGenerate?.setOnClickListener {

            val intent = Intent(this@UserWiseReportActivity,ReportGenerateActivity::class.java)
            intent.putExtra("date", binding!!.edittextSelectDate.text.toString())
            startActivity(intent)

        }
    }
}