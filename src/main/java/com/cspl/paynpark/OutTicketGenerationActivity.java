package com.cspl.paynpark;

import static java.lang.Math.ceil;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cspl.paynpark.api.Api;
import com.cspl.paynpark.databinding.ActivityOutTicketGenerationBinding;
import com.cspl.paynpark.dbhelper.AppDatabase;
import com.cspl.paynpark.model.HeaderFooter;
import com.cspl.paynpark.model.VehicFare;
import com.cspl.paynpark.model.VehicType;
import com.cspl.paynpark.print.PrinterHelper;
import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.printer.AlignStyle;
import com.ftpos.library.smartpos.printer.OnPrinterCallback;
import com.ftpos.library.smartpos.printer.PrintStatus;
import com.ftpos.library.smartpos.printer.Printer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
        int paid = b.getInt("paid", 0);
        int amt = b.getInt("amt", 0);

        init(recpNo, date, vehNo, vehType, outTime, paid, amt);

        this.printer = MainActivity.printer;
        printerHelper = new PrinterHelper(printer);
    }

    public void init(String recpNo, String date, String vehNo, String vehType, String outTime, int paid, int amt) {
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
        binding.textPaid.setText(""+paid);
        binding.textAmount.setText(""+amt);

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

            // ---- Print Strings ----
            printerHelper.printText(""+binding.textHeader1.getText(), AlignStyle.PRINT_STYLE_CENTER);
            printerHelper.printText("Receipt No : " + binding.textTicketNo.getText(), AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("Date       : " + binding.textDate.getText(), AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("Vehicle No : " + binding.textVehicleNo.getText(), AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("Vehicle Type : " + binding.textVehicleType.getText(), AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("Out Time : " + binding.textOuttime.getText(), AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("Paid : " + binding.textPaid.getText(), AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("Diff.amount : ₹" + binding.textAmount.getText(), AlignStyle.PRINT_STYLE_LEFT);

            // ---- Footer ----
            if(binding.textFooter1.length() == 0) {
                printerHelper.printFooter("**** Thank You Visit Again ****","");
            }else{
                printerHelper.printFooter(binding.textFooter1.getText().toString(), "");
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
        super.onBackPressed();
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
