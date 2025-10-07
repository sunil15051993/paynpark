package com.cspl.paynpark;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.cspl.paynpark.model.TicketReport;
import com.cspl.paynpark.model.TotalColReport;
import com.cspl.paynpark.model.VehicFare;
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
    private ProgressDialog pdDialog;
    private SharedPreferences myPref;
    private AppDatabase db;
    String serialNo, last4;
    private List<TotalColReport> reportList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTicketInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        myPref = getSharedPreferences("paynpark", MODE_PRIVATE);
        boolean call = myPref.getBoolean("api_vh_type", false);
        db = AppDatabase.getInstance(TicketInActivity.this);

        try {
            // get system serial
            serialNo = android.os.Build.getSerial();
            Log.e("POS_SN", "Serial Number: " + serialNo);
            last4 = serialNo.substring(serialNo.length() - 6);
        } catch (SecurityException e) {
            e.printStackTrace();
            Log.e("POS_SN", "Permission required to read serial");
        }

        init();

        if (!call) {
            callVehicleType();
        }
    }

    public void init() {
        Executors.newSingleThreadExecutor().execute(() -> {
            TotalColReport totalColReport = db.ticketDao().getTotalData();

            runOnUiThread(() -> {
                int noOfVeh = totalColReport != null ? totalColReport.totalVehicles : 0;
                int totalCol = (totalColReport != null && totalColReport.totalCollection != null)
                        ? totalColReport.totalCollection : 0;

                binding.textTotalVehicle.setText("Total Vehicle : " + noOfVeh);
                binding.textTotalCollection.setText("Total Collection : " + totalCol);
            });
        });
        binding.imageRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callVehicleType();
            }
        });
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
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

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        int displayHour = hour % 12;
        if (displayHour == 0) {
            displayHour = 12;
        }
        String amPm = (hour < 12) ? "AM" : "PM";

        String currentTime = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm);
        binding.edittextInTime.setText(currentTime);

//        binding.edittextInTime.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Calendar calendar = Calendar.getInstance();
//                int hour = calendar.get(Calendar.HOUR_OF_DAY);
//                int minute = calendar.get(Calendar.MINUTE);
//
//                // Create TimePickerDialog
//                TimePickerDialog timePickerDialog = new TimePickerDialog(TicketInActivity.this,
//                        new TimePickerDialog.OnTimeSetListener() {
//                            @Override
//                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                                // Format time (HH:mm)
//                                int hour = hourOfDay % 12;
//                                if (hour == 0) {
//                                    hour = 12; // show 12 instead of 0
//                                }
//                                String amPm = (hourOfDay < 12) ? "AM" : "PM";
//
//                                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm);
//                                binding.edittextInTime.setText(selectedTime);
//                            }
//                        },
//                        hour,
//                        minute,
//                        false
//                );
//
//                timePickerDialog.show();
//            }
//        });

        binding.buttonGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.edittextVhicleNo.length() == 0) {
                    binding.edittextVhicleNo.requestFocus();
                    binding.edittextVhicleNo.setError("FIELD CANNOT BE EMPTY");
                } else if (binding.spinnerVehicleType.length() == 0) {
                    binding.spinnerVehicleType.requestFocus();
                    binding.spinnerVehicleType.setError("FIELD CANNOT BE EMPTY");
                } else if (binding.edittextInTime.length() == 0) {
                    binding.edittextInTime.requestFocus();
                    binding.edittextInTime.setError("FIELD CANNOT BE EMPTY");
                } else {
                    String date = binding.edittextDate.getText().toString();
                    String ticketNo = generateTicketNo(TicketInActivity.this, date);
                    Log.e("TicketNumber", ticketNo);
                    String vehicleNo = binding.edittextVhicleNo.getText().toString();
                    String vehicleType = binding.spinnerVehicleType.getText().toString();
                    String durationType = binding.spinnerDuration.getText().toString();
                    String inTime = binding.edittextInTime.getText().toString();

                    int hours = 1;
                    try {
                        hours = Integer.parseInt(durationType.replaceAll("[^0-9]", ""));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    int finalHours = hours;
                    Executors.newSingleThreadExecutor().execute(() -> {
                        VehicFare fare = db.fareDao().getFarePrice(vehicleType, finalHours); // match vehicleType & hours

                        runOnUiThread(() -> {
                            if (fare != null) {
                                int amtPerHr = fare.getPrice();
                                Log.e("TICKET_IN", "Type: " + vehicleType + " Hours: 1 → Price: " + amtPerHr);

                                // 👉 Now create and insert ticket AFTER amtPerHr is ready
                                Ticket contact = new Ticket(ticketNo, date, vehicleNo, vehicleType, durationType, inTime, "", amtPerHr, "abcd");
                                db = AppDatabase.getInstance(TicketInActivity.this);
                                Executors.newSingleThreadExecutor().execute(() -> db.ticketDao().insert(contact));

                                Intent generate = new Intent(TicketInActivity.this, InTicketGenerationActivity.class);
                                generate.putExtra("receipt_no", ticketNo);
                                generate.putExtra("date", date);
                                generate.putExtra("vehicle_no", vehicleNo);
                                generate.putExtra("vehicle_type", vehicleType);
                                generate.putExtra("in_time", inTime);
                                generate.putExtra("amt_per_hr", amtPerHr);
                                generate.putExtra("s_n", last4);
                                startActivity(generate);
                                finish();

                            } else {
                                Toast.makeText(TicketInActivity.this, "No fare found!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            }
        });
    }

    private String generateTicketNo(Context context, String currentDate) {
        SharedPreferences prefs = context.getSharedPreferences("TicketPrefs", MODE_PRIVATE);

        // Get last stored date and ticket no
        String lastDate = prefs.getString("last_date", "");
        int lastTicketNo = prefs.getInt("last_ticket_no", 0);

        int newTicketNo;
        if (currentDate.equals(lastDate)) {
            newTicketNo = lastTicketNo + 1;
        } else {
            newTicketNo = 1;
        }

        // Save back to prefs
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("last_date", currentDate);
        editor.putInt("last_ticket_no", newTicketNo);
        editor.apply();

        // Format with leading zeros (0001, 0002…)
        return String.format("%04d", newTicketNo);
    }

    private void callVehicleType() {
        pdDialog = new ProgressDialog(TicketInActivity.this);
        pdDialog.setTitle("Please wait...");
        pdDialog.setCancelable(false);

        String url = Api.VEHICLE_TYPE;

        pdDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
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
                                SharedPreferences.Editor editor = myPref.edit();
                                editor.putBoolean("api_vh_type", true);
                                editor.apply();

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
