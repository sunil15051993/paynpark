package com.cspl.paynpark;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.cspl.paynpark.databinding.ActivityOutTicketGenerationBinding;
import com.cspl.paynpark.dbhelper.AppDatabase;
import com.cspl.paynpark.model.HeaderFooter;
import com.cspl.paynpark.print.PrinterHelper;
import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.printer.AlignStyle;
import com.ftpos.library.smartpos.printer.PrintStatus;
import com.ftpos.library.smartpos.printer.Printer;

import java.util.concurrent.Executors;

public class OutTicketGenerationActivity extends AppCompatActivity {
    private ActivityOutTicketGenerationBinding binding;
    private Printer printer;
    private PrinterHelper printerHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOutTicketGenerationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle b = getIntent().getExtras();
        String recpNo = b.getString("receipt_no", "");
        String date = b.getString("date", "");
        String vehNo = b.getString("vehicle_no", "");
        String vehType = b.getString("vehicle_type", "");
        String outTime = b.getString("out_time", "");
        String totalHrs = b.getString("total_hrs", "");
        String inTime = b.getString("in_time", "");
        String serial = b.getString("s_n", "");
        int paid = b.getInt("paid", 0);
        int amt = b.getInt("amt", 0);

        init(recpNo, date, vehNo, vehType, outTime, inTime, paid, amt, totalHrs,serial);

        this.printer = MainActivity.printer;
        printerHelper = new PrinterHelper(printer);
    }

    public void init(String recpNo, String date, String vehNo, String vehType, String outTime, String inTime, int paid, int amt, String totalHrs, String serial) {
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(OutTicketGenerationActivity.this)
                        .setTitle("Exit")
                        .setMessage("Do you want to exit?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", (dialog, which) -> {
                            callBack();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            }
        });

        AppDatabase db = AppDatabase.getInstance(OutTicketGenerationActivity.this);

        binding.textTicketNo.setText(recpNo);
        binding.textDate.setText(date);
        binding.textVehicleNo.setText(vehNo);
        binding.textVehicleType.setText(vehType);
        binding.textOuttime.setText(outTime);
        binding.textInTime.setText(inTime);
        binding.textPaid.setText(""+paid);
        binding.textAmount.setText(""+amt);
        binding.textTotalHrs.setText(totalHrs);
        binding.textSerialNo.setText(serial);

        Executors.newSingleThreadExecutor().execute(() -> {
            HeaderFooter hf = db.headerFooterDao().getHeaderFooter();

            if (hf != null) {
                runOnUiThread(() -> {

                    String headerText = hf.getH1() + "\n" + hf.getH2() + "\n" + hf.getH3() + "\n" + hf.getH4();
                    String footerText = hf.getF1() + "\n" + hf.getF2() + "\n" + hf.getF3() + "\n" + hf.getF4();

                    binding.textHeader1.setText(headerText);
                    binding.textFooter1.setText(footerText);
                });
            }
        });

        binding.buttongetPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printReceipt();
            }
        });
    }

    private void printReceipt() {
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

            String leftText = "Receipt No : " + binding.textTicketNo.getText().toString();
            String rightText = "S/N : " + binding.textSerialNo.getText().toString();
            String inDt = "IN TM: " + binding.textInTime.getText().toString();
            String inTime = "OUT TM: " + binding.textOuttime.getText().toString();

            int lineChars = 32;
            int spaceCount = lineChars - leftText.length() - rightText.length();
            if (spaceCount < 0) spaceCount = 0; // Prevent negative spaces
            String spaces = new String(new char[spaceCount]).replace('\0', ' ');
            String receiptLine = leftText + spaces + rightText;

            int spaceCount2 = lineChars - inDt.length() - inTime.length();
            if (spaceCount2 < 0) spaceCount2 = 0; // Prevent negative spaces
            String spaces2 = new String(new char[spaceCount2]).replace('\0', ' ');
            String dateLine = inDt + spaces2 + spaces2 + inTime;

            // ---- Print Strings ----
            printerHelper.printText(""+binding.textHeader1.getText(), AlignStyle.PRINT_STYLE_CENTER);
            printerHelper.printText("---------------------------------------", AlignStyle.PRINT_STYLE_CENTER);
            printerHelper.printText(receiptLine, AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("Date       : " + binding.textDate.getText(), AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printLargeText("Vehicle No : " + binding.textVehicleNo.getText().toString(), 30);
            printerHelper.printText("Vehicle Type : " + binding.textVehicleType.getText(), AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText(dateLine, AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("Total Hrs : " + binding.textTotalHrs.getText(), AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("Paid : " + binding.textPaid.getText(), AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printLargeText("Diff.amount : ₹ " + binding.textAmount.getText(), 30);
            printerHelper.printText("---------------------------------------", AlignStyle.PRINT_STYLE_CENTER);

            // ---- Footer ----
            if(binding.textFooter1.length() == 0) {
                printerHelper.printFooter("**** Thank You Visit Again ****");
            }else{
                printerHelper.printFooter(binding.textFooter1.getText().toString());
            }

            // ---- Finish Printing ----
            printerHelper.finishPrint();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PRINT", "print failed: " + e.getMessage());
        }
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Do you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    callBack();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    private void callBack() {
        Intent dash = new Intent(OutTicketGenerationActivity.this, DashboardActivity.class);
        startActivity(dash);
        finish();
    }
}
