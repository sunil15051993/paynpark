package com.cspl.paynpark

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cspl.paynpark.databinding.ActivityDateReportBinding
import java.util.Calendar

class DateWiseReportActivity : AppCompatActivity() {
    private var binding: ActivityDateReportBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDateReportBinding.inflate(layoutInflater)
        setContentView(binding!!.getRoot())

        initCall()
    }

    private fun initCall(){
        binding!!.imageBack.setOnClickListener {
            onBackPressed()
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

            val intent = Intent(this@DateWiseReportActivity,ReportGenerateActivity::class.java)
            intent.putExtra("date", binding!!.edittextSelectDate.text.toString())
            startActivity(intent)

        }
    }
}