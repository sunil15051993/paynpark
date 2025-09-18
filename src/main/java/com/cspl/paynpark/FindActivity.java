package com.cspl.paynpark;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cspl.paynpark.databinding.ActivityFindBinding;
import com.cspl.paynpark.databinding.ActivityTicketInBinding;
import com.cspl.paynpark.dbhelper.AppDatabase;
import com.cspl.paynpark.model.Ticket;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;

public class FindActivity extends AppCompatActivity {
    private ActivityFindBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFindBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();

    }

    private void initView() {
        binding.edittextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get current date
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Open DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        v.getContext(),
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            Calendar selectedDate = Calendar.getInstance();
                            selectedDate.set(selectedYear, selectedMonth, selectedDay);

                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            String formattedDate = sdf.format(selectedDate.getTime());

                            binding.edittextDate.setText(formattedDate);
                        },
                        year, month, day
                );

                datePickerDialog.show();
            }
        });
        binding.buttonFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.edittextVhicleNo.getText().toString();
                binding.edittextDate.getText().toString();
                AppDatabase db = AppDatabase.getInstance(FindActivity.this);
                if (binding.edittextVhicleNo.getText().toString().isEmpty() || binding.edittextDate.getText().toString().isEmpty()) {
                    Toast.makeText(FindActivity.this, "Please enter vehicle no. and date", Toast.LENGTH_SHORT).show();
                    return;
                }

                Executors.newSingleThreadExecutor().execute(() -> {
                    Ticket ticket = db.ticketDao().getTicketByVehicleAndDate(binding.edittextVhicleNo.getText().toString(), binding.edittextDate.getText().toString());

                    runOnUiThread(() -> {
                        if (ticket != null) {
                            Intent i = new Intent(FindActivity.this,TicketOutActivity.class);
                            i.putExtra("recpNo", ticket.getTicketNo());
                            i.putExtra("date", ticket.getDate());
                            i.putExtra("vehNo", ticket.getVehicleNo());
                            i.putExtra("vehType", ticket.getVehicleType());
                            i.putExtra("inTime", ticket.getInTime());
                            startActivity(i);
                            Log.e("FIND_ENTRY", "onClick: "+  "Vehicle: " + ticket.getVehicleNo() +
                                    "\nDate: " + ticket.getDate() +
                                    "\nInTime: " + ticket.getInTime());
                        } else {
                            Toast.makeText(FindActivity.this, "No record found", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });
    }
}