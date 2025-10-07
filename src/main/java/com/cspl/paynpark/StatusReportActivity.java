package com.cspl.paynpark;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.cspl.paynpark.databinding.ActivityFindBinding;
import com.cspl.paynpark.databinding.ActivityStatusReportBinding;
import com.cspl.paynpark.dbhelper.AppDatabase;
import com.cspl.paynpark.model.HeaderFooter;
import com.cspl.paynpark.model.StatusReport;
import com.cspl.paynpark.model.TicketReport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class StatusReportActivity extends AppCompatActivity {
    private ActivityStatusReportBinding binding;
    private List<StatusReport> reportList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatusReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();

    }

    private void initView() {
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        // Get current date & time
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(now);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = timeFormat.format(now);

        binding.textDate.setText(currentDate);
        binding.textTime.setText(currentTime);

        AppDatabase db = AppDatabase.getInstance(StatusReportActivity.this);
        Executors.newSingleThreadExecutor().execute(() -> {
            reportList = db.ticketDao().getStatusByDate(currentDate);

            runOnUiThread(() -> {
                StringBuilder srNo = new StringBuilder();
                StringBuilder vehicleTypes = new StringBuilder();
                StringBuilder inCount = new StringBuilder();
                StringBuilder outCount = new StringBuilder();
                StringBuilder totalAmts = new StringBuilder();

                int index = 1;
                int grandTotal = 0;
                for (StatusReport report : reportList) {
                    srNo.append(index).append("\n");
                    vehicleTypes.append(report.vehicleType).append("\n");
                    inCount.append(report.inCount).append("\n");
                    outCount.append(report.outCount).append("\n");
                    totalAmts.append(report.totalAmt).append("\n");
                    grandTotal += report.totalAmt;
                    index++;
                }

                binding.textSrNo.setText(srNo.toString());
                binding.textVehicleType.setText(vehicleTypes.toString());
                binding.textInNo.setText(inCount.toString());
                binding.textOutNo.setText(outCount.toString());
//                binding.textTotalCol.setText(String.valueOf(grandTotal));
            });
        });

        Executors.newSingleThreadExecutor().execute(() -> {
            HeaderFooter hf = db.headerFooterDao().getHeaderFooter();

            if (hf != null) {
                runOnUiThread(() -> {

                    String headerText = hf.getH1() + "\n" + hf.getH2() + "\n" + hf.getH3() + "\n" + hf.getH4();
                    String footerText = hf.getF1() + "\n" + hf.getF2() + "\n" + hf.getF3() + "\n" + hf.getF4();

                    binding.textHeader1.setText(headerText);
                });
            }
        });
    }
}
