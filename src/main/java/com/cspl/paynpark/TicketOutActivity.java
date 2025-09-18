package com.cspl.paynpark;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.cspl.paynpark.databinding.ActivityTicketOutBinding;
import com.cspl.paynpark.dbhelper.AppDatabase;
import com.cspl.paynpark.model.Ticket;
import com.cspl.paynpark.model.VehicFare;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicketOutActivity extends AppCompatActivity {
    private ActivityTicketOutBinding binding;
    List<String> vehicleType = Arrays.asList(
            "Two Wheeler", "Four Wheeler", "Heavy Vehicle");
    List<String> durationType = Arrays.asList(
            "Hourly", "Daily", "Monthly");
    int ticketCounter = 0;
    private int totalPrice;
    private ProgressDialog pdDialog;
    private SharedPreferences myPref;

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

        myPref = getSharedPreferences("paynpark", MODE_PRIVATE);
        boolean call = myPref.getBoolean("api_vh_fare",false);

        if(!call) {
            callGetPrice();
        }
        init(recpNo,date,vehNo,vehType,inTime);
    }

    private void callGetPrice() {
        pdDialog = new ProgressDialog(TicketOutActivity.this);
        pdDialog.setTitle("Please wait...");
        pdDialog.setCancelable(false);

        String url = Api.VEHICLE_FARE;

        pdDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET,url + "?username=abc",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pdDialog.dismiss();
                        try {
                            Log.e("res_type", response);
                            Log.e("res_type", url);

                            JSONObject rootObj = new JSONObject(response);

                            JSONObject faresMatrix = rootObj.getJSONObject("fares_matrix");

                            AppDatabase db = AppDatabase.getInstance(TicketOutActivity.this);
                            ExecutorService executor = Executors.newSingleThreadExecutor();

                            // Loop through vehicle types (keys)
                            Iterator<String> keys = faresMatrix.keys();
                            while (keys.hasNext()) {
                                String vehicleType = keys.next();
                                JSONArray faresArray = faresMatrix.getJSONArray(vehicleType);

                                for (int i = 0; i < faresArray.length(); i++) {
                                    JSONObject fareObj = faresArray.getJSONObject(i);

                                    int hours = fareObj.getInt("hours");
                                    int price = fareObj.getInt("price");

                                    VehicFare fare = new VehicFare(vehicleType, hours, price);
                                    executor.execute(() -> db.fareDao().insert(fare));
                                    SharedPreferences.Editor editor = myPref.edit();
                                    editor.putBoolean("api_vh_fare",true);
                                    editor.apply();
                                }
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
                Map<String, String> params = new HashMap<>();
                params.put("username","abc");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(TicketOutActivity.this);
        requestQueue.add(stringRequest);

    }

    public void init(String recpNo, String inDate, String vehNo, String vehType, String inTime){
        binding.imageRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callGetPrice();
            }
        });
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
                AppDatabase db = AppDatabase.getInstance(TicketOutActivity.this);

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
                        VehicFare fare = db.fareDao().getFareFor(vehType, (int) diffHours);
                        int pricePerHour = 10;
                        if (fare != null) {
                            pricePerHour = fare.getPrice();   // set db price
                        } else {
                            Log.e("fare_check", "No fare found for " + vehType + " at " + diffHours + " hours");
                        }

                        totalPrice = (int) (diffHours * pricePerHour);
                        Log.e("fare_result", "Total: " + totalPrice);
                        totalPrice = (int) diffHours * pricePerHour;

                        String totalHrs = diffHours + " hrs";
                        String result = "Total Time: " + totalHrs + " | Total Price: ₹" + totalPrice;

                        Log.e("TICKET_OUT", "onClick: " + result);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

//                AppDatabase db = AppDatabase.getInstance(TicketOutActivity.this);
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
