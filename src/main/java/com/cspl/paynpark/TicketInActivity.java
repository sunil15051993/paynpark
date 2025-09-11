package com.cspl.paynpark;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cspl.paynpark.databinding.ActivityTicketInBinding;
import com.cspl.paynpark.dbhelper.AppDatabase;
import com.cspl.paynpark.model.Ticket;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class TicketInActivity extends AppCompatActivity {
    private ActivityTicketInBinding binding;
    List<String> vehicleType = Arrays.asList(
            "Two Wheeler", "Four Wheeler", "Heavy Vehicle");
    List<String> durationType = Arrays.asList(
            "Hourly", "Daily", "Monthly");
    int ticketCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTicketInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    public void init(){
        ArrayAdapter<String> adapterVehicle = new ArrayAdapter<>(TicketInActivity.this, android.R.layout.simple_dropdown_item_1line, vehicleType);
        binding.spinnerVehicleType.setAdapter(adapterVehicle);

        ArrayAdapter<String> adapterDuration = new ArrayAdapter<>(TicketInActivity.this, android.R.layout.simple_dropdown_item_1line, durationType);
        binding.spinnerDuration.setAdapter(adapterDuration);

        //current date
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        binding.edittextDate.setText(currentDate);

        binding.edittextInTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                // Create TimePickerDialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(TicketInActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                // Format time (HH:mm)
                                int hour = hourOfDay % 12;
                                if (hour == 0) {
                                    hour = 12; // show 12 instead of 0
                                }
                                String amPm = (hourOfDay < 12) ? "AM" : "PM";

                                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm);
                                binding.edittextInTime.setText(selectedTime);
                            }
                        },
                        hour,
                        minute,
                        false
                );

                timePickerDialog.show();
            }
        });

        binding.buttonGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = binding.edittextDate.getText().toString();
                Random random = new Random();
                int randomNumber = random.nextInt(1000);  // generates 0–999
                String ticketNo = "PARK" + randomNumber;
                Log.e("TicketNumber", ticketNo);
                String vehicleNo = binding.edittextVhicleNo.getText().toString();
                String vehicleType = binding.spinnerVehicleType.getText().toString();
                String durationType = binding.spinnerDuration.getText().toString();
                String inTime = binding.edittextInTime.getText().toString();

                Ticket contact = new Ticket(ticketNo,date,vehicleNo,vehicleType,durationType,inTime,"",0);
                AppDatabase db = AppDatabase.getInstance(TicketInActivity.this);
                db.ticketDao().insert(contact);
                Intent generate = new Intent(TicketInActivity.this, InTicketGenerationActivity.class);
                generate.putExtra("receipt_no",ticketNo);
                generate.putExtra("date",date);
                generate.putExtra("vehicle_no",vehicleNo);
                generate.putExtra("vehicle_type",vehicleType);
                generate.putExtra("in_time",inTime);
                startActivity(generate);
                finish();

                Toast.makeText(TicketInActivity.this, "Data Saved!", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
