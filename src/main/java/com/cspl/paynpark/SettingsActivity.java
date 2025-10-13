package com.cspl.paynpark;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cspl.paynpark.api.Api;
import com.cspl.paynpark.databinding.ActivityFindBinding;
import com.cspl.paynpark.databinding.ActivitySettingsBinding;
import com.cspl.paynpark.dbhelper.AppDatabase;
import com.cspl.paynpark.model.HeaderFooter;
import com.cspl.paynpark.model.LoginMaster;
import com.cspl.paynpark.model.VehicFare;
import com.cspl.paynpark.model.VehicType;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private ProgressDialog pdDialog;
    private SharedPreferences myPref;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = AppDatabase.getInstance(SettingsActivity.this);

        initView();

    }

    private void initView() {
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        binding.buttonLoginSynch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callLoginMaster();
            }
        });

        binding.buttonVehicleSynch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callVehicleType();
            }
        });

        binding.buttonFareSynch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callGetPrice();
            }
        });

        binding.buttonHeaderSynch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callGetHeaderFooter();
            }
        });
    }

    private void callLoginMaster() {
        pdDialog = new ProgressDialog(SettingsActivity.this);
        pdDialog.setTitle("Please wait...");
        pdDialog.setCancelable(false);

        String url = Api.LOGIN_MASTER;

        pdDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pdDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            boolean status = jsonObject.getBoolean("status");
                            String message = jsonObject.getString("message");

                            if (status) {

                                JSONObject userObj = jsonObject.getJSONObject("user");
                                String name = userObj.getString("name");
                                String serialNumber = userObj.getString("serial_number");
                                String position = userObj.getString("position");


                                //Get POS Users Array
                                JSONArray posUsersArray = jsonObject.getJSONArray("pos_users");

                                for (int i = 0; i < posUsersArray.length(); i++) {
                                    JSONObject recordObj = posUsersArray.getJSONObject(i);

                                    String userName = recordObj.getString("name");
                                    String userPwd = recordObj.getString("password");
                                    String userPosition = recordObj.getString("position");

                                    LoginMaster loginMaster = new LoginMaster(userName, userPwd, userPosition, serialNumber, "", "");
                                    Executors.newSingleThreadExecutor().execute(() -> db.loginDao().insert(loginMaster));
                                }

                                Log.d("API", "Admin and POS users inserted successfully");
                                Toast.makeText(SettingsActivity.this, "Successfully Updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SettingsActivity.this, "Not Updated", Toast.LENGTH_SHORT).show();
                                Log.e("API", "Login failed: " + message);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("API Error", "Exception: " + e.getMessage());
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
                params.put("name", "Cursor Enterprice");
                params.put("serial_number", "525788");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(SettingsActivity.this);
        requestQueue.add(stringRequest);

    }

    private void callVehicleType() {
        pdDialog = new ProgressDialog(SettingsActivity.this);
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
                                AppDatabase db = AppDatabase.getInstance(SettingsActivity.this);
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

        RequestQueue requestQueue = Volley.newRequestQueue(SettingsActivity.this);
        requestQueue.add(stringRequest);

    }

    private void callGetPrice() {
        pdDialog = new ProgressDialog(SettingsActivity.this);
        pdDialog.setTitle("Please wait...");
        pdDialog.setCancelable(false);

        String url = Api.VEHICLE_FARE;
        Log.e("res_type", url);
        pdDialog.show();

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pdDialog.dismiss();
                        try {
                            Log.e("res_type", response);
                            Log.e("res_type", url);

                            JSONObject rootObj = new JSONObject(response);
                            JSONObject faresMatrix = rootObj.getJSONObject("fares_matrix");

                            AppDatabase db = AppDatabase.getInstance(SettingsActivity.this);
                            Executor executor = Executors.newSingleThreadExecutor();

                            Iterator<String> keys = faresMatrix.keys();
                            while (keys.hasNext()) {
                                String vehicleType = keys.next();
                                JSONArray faresArray = faresMatrix.getJSONArray(vehicleType);

                                for (int i = 0; i < faresArray.length(); i++) {
                                    JSONObject fareObj = faresArray.getJSONObject(i);

                                    int hours = fareObj.getInt("hours");
                                    int price = fareObj.getInt("price");

                                    VehicFare fare = new VehicFare(vehicleType, (long) hours, price);
                                    executor.execute(() -> db.fareDao().insert(fare));

                                    SharedPreferences.Editor editor = myPref.edit();
                                    editor.putBoolean("api_vh_fare", true);
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
                        Log.e("RequestError", "Registration Error: " + error);
                        Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", "abcd");
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(SettingsActivity.this);
        requestQueue.add(stringRequest);
    }

    private void callGetHeaderFooter() {
        String url = Api.HEADER_FOOTER;
        Log.e("header_api", url);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.e("header_res", response);
                            JSONObject rootObj = new JSONObject(response);

                            if (rootObj.getBoolean("status")) {
                                JSONObject dataObj = rootObj.getJSONObject("data");

                                JSONObject headerObj = dataObj.getJSONObject("header");
                                String header1 = headerObj.optString("header1", "Pay & Park");
                                String header2 = headerObj.optString("header2", "");
                                String header3 = headerObj.optString("header3", "");
                                String header4 = headerObj.optString("header4", "");

                                JSONObject footerObj = dataObj.getJSONObject("footer");
                                String footer1 = footerObj.optString("footer1", "Thank You visit again");
                                String footer2 = footerObj.optString("footer2", "");
                                String footer3 = footerObj.optString("footer3", "");
                                String footer4 = footerObj.optString("footer4", "");

                                // Save into DB (Room)
                                AppDatabase db = AppDatabase.getInstance(SettingsActivity.this);
                                Executors.newSingleThreadExecutor().execute(() -> {
                                    HeaderFooter hf = new HeaderFooter(
                                            header1, header2, header3, header4,
                                            footer1, footer2, footer3, footer4
                                    );
                                    db.headerFooterDao().insert(hf);
                                });
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("header_error", "Error: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("header_request", "Error: " + error);
                        Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", "abcd");
                return params;
            }
        };

        Volley.newRequestQueue(SettingsActivity.this).add(stringRequest);
    }

}
