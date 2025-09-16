package com.cspl.paynpark;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cspl.paynpark.api.Api;
import com.cspl.paynpark.databinding.ActivityTicketInBinding;
import com.cspl.paynpark.dbhelper.AppDatabase;
import com.cspl.paynpark.model.Ticket;
import com.cspl.paynpark.model.VehicType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicketInActivity extends AppCompatActivity {
    private ActivityTicketInBinding binding;
//    List<String> vehicleType = Arrays.asList(
//            "Two Wheeler", "Four Wheeler", "Heavy Vehicle");
    List<String> durationType = Arrays.asList(
            "Hourly", "Daily", "Monthly");
    int ticketCounter = 0;
    private ProgressDialog pdDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTicketInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        callVehicleType();
    }

    public void init(){
        AppDatabase db = AppDatabase.getInstance(this);

        db.typeDao().getAllTypes().observe(this, typeList -> {
            List<String> vehicleTypes = new ArrayList<>();
            for (VehicType t : typeList) {
                vehicleTypes.add(t.getVehicleType());
            }

            ArrayAdapter<String> adapterVehicle = new ArrayAdapter<>(TicketInActivity.this,
                    android.R.layout.simple_dropdown_item_1line,
                    vehicleTypes
            );
            binding.spinnerVehicleType.setAdapter(adapterVehicle);
        });

//        ArrayAdapter<String> adapterVehicle = new ArrayAdapter<>(TicketInActivity.this, android.R.layout.simple_dropdown_item_1line, vehicleType);
//        binding.spinnerVehicleType.setAdapter(adapterVehicle);

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

    private void callVehicleType() {
        pdDialog = new ProgressDialog(TicketInActivity.this);
        pdDialog.setTitle("Please wait...");
        pdDialog.setCancelable(false);

        String url = Api.VEHICLE_TYPE;

        pdDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET,url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pdDialog.dismiss();
                        try {
                            Log.e("res_type", response);
                            JSONArray recordsArray = new JSONArray(response);

                            for (int i = 0; i < recordsArray.length(); i++) {
                                JSONObject recordObj = recordsArray.getJSONObject(i);

                                int id = recordObj.getInt("id");
                                String vehicleType = recordObj.getString("vehicle_type");

                                VehicType types = new VehicType(vehicleType);
                                AppDatabase db = AppDatabase.getInstance(TicketInActivity.this);
                                db.typeDao().insert(types);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("category_error", "Error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pdDialog.dismiss();
                        Log.e("RequestError", "Registration Error: " + error.toString());
                        Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                return new HashMap<>();
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(TicketInActivity.this);
        requestQueue.add(stringRequest);

    }
}
