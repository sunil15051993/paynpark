package com.cspl.paynpark;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cspl.paynpark.api.Api;
import com.cspl.paynpark.databinding.ActivityLoginBinding;
import com.cspl.paynpark.dbhelper.AppDatabase;
import com.cspl.paynpark.model.LoginMaster;
import com.cspl.paynpark.model.VehicFare;
import com.cspl.paynpark.model.VehicType;
import com.ftpos.library.smartpos.printer.Printer;
import com.ftpos.library.smartpos.servicemanager.OnServiceConnectCallback;
import com.ftpos.library.smartpos.servicemanager.ServiceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    public static Printer printer = null;
    private Context mContext;
    private ProgressDialog pdDialog;
    private SharedPreferences myPref;
    private AppDatabase db;
    private boolean call;
    List<String> loginType = Arrays.asList("Operator", "Supervisor", "Admin");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mContext = this;

        ServiceManager.bindPosServer(this, new OnServiceConnectCallback() {
            @Override
            public void onSuccess() {
                printer = Printer.getInstance(mContext);
            }

            @Override
            public void onFail(int var1) {
                Log.e("binding", "onFail");
            }
        });

        myPref = getSharedPreferences("paynpark", MODE_PRIVATE);
        call = myPref.getBoolean("api_login", false);
        db = AppDatabase.getInstance(LoginActivity.this);
        Log.e("LOGIN", "onCreate: "+ call);

        init();

    }

    private void init() {
        ArrayAdapter<String> adapterDuration = new ArrayAdapter<>(LoginActivity.this, android.R.layout.simple_dropdown_item_1line, loginType);
        binding.spinnerType.setAdapter(adapterDuration);
        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.edittextUser.length() == 0){
                    binding.edittextUser.requestFocus();
                    binding.edittextUser.setError("FIELD CANNOT BE EMPTY");
                } else if (binding.edittextPass.length() == 0) {
                    binding.edittextPass.requestFocus();
                    binding.edittextPass.setError("FIELD CANNOT BE EMPTY");
                }else {
                    if (!call) {
                        callLoginApi();
                    } else {
                        String selectedType = binding.spinnerType.getText().toString().trim();
                        Log.e("LOGIN", "onClick: "+ selectedType);
                        if (selectedType.equalsIgnoreCase("Admin")) {
                            callLoginApi();
                        } else {
                            callLoginDB();
                        }
                    }
                }
            }
        });
    }

    private void callLoginApi() {
        pdDialog = new ProgressDialog(LoginActivity.this);
        pdDialog.setTitle("Please wait...");
        pdDialog.setCancelable(false);

        String url = Api.SIGN_IN;
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
                            Log.e("res_login", response);
                            Log.e("res_login", url);

                            JSONObject rootObj = new JSONObject(response);
                            boolean status = rootObj.getBoolean("status");
                            String message = rootObj.getString("message");

                            if (status) {
                                JSONObject userObj = rootObj.getJSONObject("user");

                                String name = userObj.optString("name", "");
                                String email = userObj.optString("email", "");
                                int companyId = userObj.optInt("company_id", 0);
                                String serialNumber = userObj.optString("serial_number", "");
                                String position = userObj.optString("position", "");
                                String type = userObj.optString("type", "");

                                if(position.equals("Admin")){
                                    startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                                }else {
                                    // Save into Room DB
                                    LoginMaster loginMaster = new LoginMaster(name, binding.edittextPass.getText().toString(), position, serialNumber, "", "");
                                    Executors.newSingleThreadExecutor().execute(() -> db.loginDao().insert(loginMaster));
                                    callLoginDB();
                                }

                                // Also save login flag in SharedPreferences
                                SharedPreferences.Editor editor = myPref.edit();
                                editor.putBoolean("api_login", true);
                                editor.apply();


                            }else {
                                Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
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
                params.put("name", binding.edittextUser.getText().toString());
                params.put("password", binding.edittextPass.getText().toString());
                params.put("serial_number", "546788");
                Log.e("res_login", "getParams: "+ params);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);
        requestQueue.add(stringRequest);
    }

    private void callLoginDB() {
        Executors.newSingleThreadExecutor().execute(() -> {
            LoginMaster loginMaster = db.loginDao().login(binding.edittextUser.getText().toString(), binding.edittextPass.getText().toString(), "546788");

            runOnUiThread(() -> {
                if (loginMaster != null) {
                    String role = loginMaster.getRole();

                    if ("Operator".equalsIgnoreCase(role)) {
                        startActivity(new Intent(this, DashboardActivity.class));
                    } else if ("Supervisor".equalsIgnoreCase(role)) {
                        startActivity(new Intent(this, SuperDashboardActivity.class));
                    } else {
                        Toast.makeText(this, "Unknown role", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    finish();
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }
}