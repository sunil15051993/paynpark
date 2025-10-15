package com.cspl.paynpark;

import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.cspl.paynpark.databinding.ActivityGenerateReportBinding;
import com.cspl.paynpark.databinding.ActivityVehicleGenerateReportBinding;
import com.cspl.paynpark.dbhelper.AppDatabase;
import com.cspl.paynpark.model.HeaderFooter;
import com.cspl.paynpark.model.TicketReport;
import com.cspl.paynpark.model.VehicType;
import com.cspl.paynpark.model.VhReport;
import com.cspl.paynpark.print.PrinterHelper;
import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.printer.AlignStyle;
import com.ftpos.library.smartpos.printer.PrintStatus;
import com.ftpos.library.smartpos.printer.Printer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class VehicleReportGenerateActivity extends AppCompatActivity {
    private ActivityVehicleGenerateReportBinding binding;
    private List<VhReport> reportList = new ArrayList<>();
    private Printer printer;
    private PrinterHelper printerHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVehicleGenerateReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle b = getIntent().getExtras();
        assert b != null;
        String date = b.getString("date","");
        String type = b.getString("veh_type","");

        Log.e("Report", "onCreate: "+ date);

        this.printer = LoginActivity.printer;
        printerHelper = new PrinterHelper(printer);

        initMethod(date,type);

    }

    private void initMethod(String date, String type) {
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

        AppDatabase db = AppDatabase.getInstance(VehicleReportGenerateActivity.this);
        Executors.newSingleThreadExecutor().execute(() -> {
            reportList = db.ticketDao().getReportByVehicle(date,type);

            runOnUiThread(() -> {
                StringBuilder srNo = new StringBuilder();
                StringBuilder vehicleTypes = new StringBuilder();
                StringBuilder inTM = new StringBuilder();
                StringBuilder outTM = new StringBuilder();

                int index = 1;
                for (VhReport report : reportList) {
                    srNo.append(index).append("\n");
                    vehicleTypes.append(report.vehicleNo).append("\n");
                    inTM.append(report.inTM).append("\n");
                    outTM.append(report.outTM).append("\n");
                    index++;
                }

                binding.textSrNo.setText(srNo.toString());
                binding.textVehicleType.setText(vehicleTypes.toString());
                binding.textInTM.setText(inTM.toString());
                binding.textOutTM.setText(outTM.toString());
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

        binding.buttongetPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printReport();
            }
        });
    }

    private void printReport() {
        try {
            int ret = printer.open();
            if (ret != ErrCode.ERR_SUCCESS) {
                Log.e("PRINT", "open failed err=0x" + ret);
                return;
            }

            printer.startCaching();
            printer.setGray(3);

            PrintStatus status = new PrintStatus();
            printer.getStatus(status);
            if (!status.getmIsHavePaper()) {
                Log.e("PRINT", "Printer out of paper");
                return;
            }

            String leftText = "DT : " + binding.textDate.getText().toString();
            String rightText = "TM : " + binding.textTime.getText().toString();

            int lineChars = 32;
            int spaceCount = lineChars - leftText.length() - rightText.length();
            if (spaceCount < 0) spaceCount = 0; // Prevent negative spaces
            String spaces = new String(new char[spaceCount]).replace('\0', ' ');
            String receiptLine = leftText + spaces + rightText;

            // ---- Print Strings ----
            printerHelper.printLargeText("VEHICLE WISE REPORT", 25, Paint.Align.CENTER);
            printerHelper.printText("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -", AlignStyle.PRINT_STYLE_CENTER);
            printerHelper.printText("" + binding.textHeader1.getText(), AlignStyle.PRINT_STYLE_CENTER);
            printerHelper.printText(receiptLine, AlignStyle.PRINT_STYLE_CENTER);
            printerHelper.printText("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -", AlignStyle.PRINT_STYLE_CENTER);
            List<String[]> rows = new ArrayList<>();
            rows.add(new String[]{"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -", "", ""});
//            printerHelper.printText("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -", AlignStyle.PRINT_STYLE_CENTER);



            // Header row
            rows.add(new String[]{"Type", "Qty", "Amt"});
            rows.add(new String[]{"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -", "", ""});

            //col
            for (VhReport report : reportList) {
                rows.add(new String[]{
                        String.valueOf(report.vehicleNo),
                        String.valueOf(report.inTM), String.valueOf(report.outTM)});
            }

//            rows.add(new String[]{"", "", ""});
            rows.add(new String[]{"- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -", "", ""});
            printerHelper.printTable(rows);

            // ---- Finish Printing ----
            printerHelper.finishPrint();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PRINT", "print failed: " + e.getMessage());
        }
    }

    private String padRight(String text, int length) {
        if (text == null) text = "";
        return String.format("%-" + length + "s", text); // left aligned
    }

    private String padLeft(String text, int length) {
        if (text == null) text = "";
        return String.format("%" + length + "s", text); // right aligned
    }
}
