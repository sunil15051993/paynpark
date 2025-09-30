package com.cspl.paynpark;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.cspl.paynpark.databinding.ActivityGenerateReportBinding;
import com.cspl.paynpark.dbhelper.AppDatabase;
import com.cspl.paynpark.model.HeaderFooter;
import com.cspl.paynpark.model.TicketReport;
import com.cspl.paynpark.print.PrinterHelper;
import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.printer.AlignStyle;
import com.ftpos.library.smartpos.printer.PrintStatus;
import com.ftpos.library.smartpos.printer.Printer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ReportGenerateActivity extends AppCompatActivity {
    private ActivityGenerateReportBinding binding;
    private List<TicketReport> reportList = new ArrayList<>();
    private Printer printer;
    private PrinterHelper printerHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGenerateReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle b = getIntent().getExtras();
        assert b != null;
        String date = b.getString("date","");
        String emp = "abcd";
        Log.e("Report", "onCreate: "+ date);

        this.printer = MainActivity.printer;
        printerHelper = new PrinterHelper(printer);

        initMethod(date,emp);

    }

    private void initMethod(String date, String emp) {
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        AppDatabase db = AppDatabase.getInstance(ReportGenerateActivity.this);
        Executors.newSingleThreadExecutor().execute(() -> {
            reportList = db.ticketDao().getReportByDate(date,emp);

            runOnUiThread(() -> {
                StringBuilder srNo = new StringBuilder();
                StringBuilder vehicleTypes = new StringBuilder();
                StringBuilder noOfTickets = new StringBuilder();
                StringBuilder totalAmts = new StringBuilder();

                int index = 1;
                int grandTotal = 0;
                for (TicketReport report : reportList) {
                    srNo.append(index).append("\n");
                    vehicleTypes.append(report.vehicleType).append("\n");
                    noOfTickets.append(report.count).append("\n");
                    totalAmts.append(report.totalAmt).append("\n");
                    grandTotal += report.totalAmt;
                    index++;
                }

                binding.textSrNo.setText(srNo.toString());
                binding.textVehicleType.setText(vehicleTypes.toString());
                binding.textNo.setText(noOfTickets.toString());
                binding.textAmt.setText(totalAmts.toString());
                binding.textUserName.setText("Staff name: " + emp);
                binding.textTotalCol.setText("Total Collection: " + String.valueOf(grandTotal));
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

        binding.buttonGenerate.setOnClickListener(new View.OnClickListener() {
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

            // ---- Print Strings ----
            printerHelper.printText("" + binding.textHeader1.getText(), AlignStyle.PRINT_STYLE_CENTER);
            String header = padRight("SrNo", 5) +
                    padRight("Type", 15) +
                    padRight("No", 5) +
                    padRight("Amt", 8);
            printerHelper.printText(header, AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("--------------------------------", AlignStyle.PRINT_STYLE_LEFT);
            // Data rows
            for (int i = 0; i < reportList.size(); i++) {
                TicketReport report = reportList.get(i);

                String row = padRight(String.valueOf(i+1), 5) +
                        padRight(report.vehicleType, 15) +
                        padRight(String.valueOf(report.count), 5) +
                        padRight(String.valueOf(report.totalAmt), 8);

                printerHelper.printText(row, AlignStyle.PRINT_STYLE_LEFT);
            }
            printerHelper.printText("--------------------------------", AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("Staff Name : " + binding.textUserName.getText(), AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("Total Collection : " + binding.textTotalCol.getText(), AlignStyle.PRINT_STYLE_LEFT);


            // ---- Finish Printing ----
            printerHelper.finishPrint();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PRINT", "print failed: " + e.getMessage());
        }
    }

    private String padRight(String text, int length) {
        return String.format("%-" + length + "s", text); // left aligned
    }

    private String padLeft(String text, int length) {
        return String.format("%" + length + "s", text); // right aligned
    }
}
