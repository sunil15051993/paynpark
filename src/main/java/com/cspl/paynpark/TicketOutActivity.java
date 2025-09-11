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
import com.cspl.paynpark.databinding.ActivityTicketOutBinding;
import com.cspl.paynpark.dbhelper.AppDatabase;
import com.cspl.paynpark.model.Ticket;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class TicketOutActivity extends AppCompatActivity {
    private ActivityTicketOutBinding binding;
    List<String> vehicleType = Arrays.asList(
            "Two Wheeler", "Four Wheeler", "Heavy Vehicle");
    List<String> durationType = Arrays.asList(
            "Hourly", "Daily", "Monthly");
    int ticketCounter = 0;
    private int totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTicketOutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle intent = getIntent().getExtras();
        String recpNo = intent.getString("recpNo");
        String date = intent.getString("date");
        String vehNo = intent.getString("vehNo");
        String vehType = intent.getString("vehType");
        String inTime = intent.getString("inTime");

        init(recpNo,date,vehNo,vehType,inTime);
    }

    public void init(String recpNo, String inDate, String vehNo, String vehType, String inTime){
       binding.edittextVhicleRec.setText(recpNo);
       binding.edittextVhicleNo.setText(vehNo);
       binding.spinnerVehicleType.setText(vehType);
       binding.edittextInTime.setText(inTime);
       binding.edittextDate.setText(inDate);

        //current date
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        binding.edittextOutDate.setText(currentDate);

        binding.edittextOutTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                // Create TimePickerDialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(TicketOutActivity.this,
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
                                binding.edittextOutTime.setText(selectedTime);
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

                String outTime = binding.edittextOutTime.getText().toString();
                String outDate = binding.edittextOutDate.getText().toString();

                try {
                    String inDateTime = inDate + " " + inTime;   // e.g. "03-09-2025 08:30 PM"
                    String outDateTime = outDate + " " + outTime; // e.g. "03-09-2025 09:30 PM"

                    // Use 12-hour format with AM/PM
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault());

                    Date inDateObj = sdf.parse(inDateTime);
                    Date outDateObj = sdf.parse(outDateTime);

                    if (inDateObj != null && outDateObj != null) {
                        long diffInMillis = outDateObj.getTime() - inDateObj.getTime();

                        long diffHours = diffInMillis / (1000 * 60 * 60);
                        long diffMinutes = (diffInMillis / (1000 * 60)) % 60;

                        if (diffMinutes > 0) {
                            diffHours += 1;  // Round up to next hour
                        }
                        // Price per hour
                        int pricePerHour = 10;
                        totalPrice = (int) diffHours * pricePerHour;

                        String totalHrs = diffHours + " hrs";
                        String result = "Total Time: " + totalHrs + " | Total Price: ₹" + totalPrice;

                        Log.e("TICKET_OUT", "onClick: " + result);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                AppDatabase db = AppDatabase.getInstance(TicketOutActivity.this);
                db.ticketDao().updateTicket(recpNo, outTime, totalPrice);
                Intent generate = new Intent(TicketOutActivity.this, OutTicketGenerationActivity.class);
                generate.putExtra("receipt_no",recpNo);
                generate.putExtra("date",outDate);
                generate.putExtra("vehicle_no",vehNo);
                generate.putExtra("vehicle_type",vehType);
                generate.putExtra("out_time",outTime);
                generate.putExtra("amt",totalPrice);
                startActivity(generate);

                Toast.makeText(TicketOutActivity.this, "Data Saved!", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
