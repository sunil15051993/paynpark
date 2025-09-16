package com.cspl.paynpark;

import static java.lang.Math.ceil;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import com.cspl.paynpark.databinding.ActivityOutTicketGenerationBinding;
import com.cspl.paynpark.dbhelper.AppDatabase;
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
        int amt = b.getInt("amt", 0);

        init(recpNo, date, vehNo, vehType, outTime, amt);

        this.printer = MainActivity.printer;
        printerHelper = new PrinterHelper(printer);
    }

    public void init(String recpNo, String date, String vehNo, String vehType, String outTime, int amt) {

        binding.textTicketNo.setText(recpNo);
        binding.textDate.setText(date);
        binding.textVehicleNo.setText(vehNo);
        binding.textVehicleType.setText(vehType);
        binding.textOuttime.setText(outTime);
        binding.textAmount.setText(""+amt);

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
            printerHelper.printText("PARKING RECEIPT", AlignStyle.PRINT_STYLE_CENTER);
            printerHelper.printText("Receipt No : " + binding.textTicketNo.getText(), AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("Date       : " + binding.textDate.getText(), AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("Vehicle No : " + binding.textVehicleNo.getText(), AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("Vehicle Type : " + binding.textVehicleType.getText(), AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("Out Time : " + binding.textOuttime.getText(), AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("Amount : ₹" + binding.textAmount.getText(), AlignStyle.PRINT_STYLE_LEFT);

            // ---- Footer ----
            printerHelper.printFooter("**** Thank You Visit Again ****", "IRCTC Parking - Surat");

            // ---- Finish Printing ----
            printerHelper.finishPrint();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PRINT", "print failed: " + e.getMessage());
        }
    }
}
