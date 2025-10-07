package com.cspl.paynpark;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.cspl.paynpark.databinding.ActivityInTicketGenerationBinding;
import com.cspl.paynpark.dbhelper.AppDatabase;
import com.cspl.paynpark.model.HeaderFooter;
import com.cspl.paynpark.print.PrinterHelper;
import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.printer.AlignStyle;
import com.ftpos.library.smartpos.printer.PrintStatus;
import com.ftpos.library.smartpos.printer.Printer;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class InTicketGenerationActivity extends AppCompatActivity {
    private ActivityInTicketGenerationBinding binding;
    private Printer printer;
    private PrinterHelper printerHelper;
    String outTime = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInTicketGenerationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle b = getIntent().getExtras();
        String recpNo = b.getString("receipt_no", "");
        String date = b.getString("date", "");
        String vehNo = b.getString("vehicle_no", "");
        String vehType = b.getString("vehicle_type", "");
        String inTime = b.getString("in_time", "");
        String serial = b.getString("s_n", "");
        int amtPerHr = b.getInt("amt_per_hr", 0);

        init(recpNo, date, vehNo, vehType, inTime, amtPerHr, serial);
        this.printer = LoginActivity.printer;
        printerHelper = new PrinterHelper(printer);
    }

    public void init(String recpNo, String inDate, String vehNo, String vehType, String inTime, int amtPerHr, String serial) {
        try {
            // Parse 12-hour format
            SimpleDateFormat inputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(inTime);

            // Convert to 24-hour format
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            outTime = outputFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        Log.e("ConvertedTime", "24hr Format: " + outTime);
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(InTicketGenerationActivity.this)
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
        AppDatabase db = AppDatabase.getInstance(InTicketGenerationActivity.this);

        binding.textTicketNo.setText(recpNo);
        binding.textDate.setText(inDate);
        binding.textVehicleNo.setText(vehNo);
        binding.textVehicleType.setText(vehType);
        binding.textIntime.setText(outTime);
        binding.textAmtPerHr.setText("" + amtPerHr);
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

        // Combine into one string
        String qrData = "Receipt No: " + recpNo + "\n"
                + "Date: " + inDate + "\n"
                + "Vehicle No: " + vehNo + "\n"
                + "Vehicle Type: " + vehType + "\n"
                + "In Time: " + inTime + "\n"
                + "Paid: " + amtPerHr;

        // Generate QR code
        QRCodeWriter writer = new QRCodeWriter();
        try {
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(qrData, BarcodeFormat.QR_CODE, 300, 300);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(bitMatrix);

            binding.textQR.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

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
            String leftText = "Bill No : " + binding.textTicketNo.getText().toString();
            String rightText = "S/N : " + binding.textSerialNo.getText().toString();
            String inDt = "IN DT: " + binding.textDate.getText().toString();
            String inTime = "IN TM: " + binding.textIntime.getText().toString();

            int lineChars = 32;
            int spaceCount = lineChars - leftText.length() - rightText.length();
            if (spaceCount < 0) spaceCount = 0; // Prevent negative spaces
            String spaces = new String(new char[spaceCount]).replace('\00', ' ');
            String receiptLine = leftText + spaces + spaces + rightText;

            int spaceCount2 = lineChars - inDt.length() - inTime.length();
            if (spaceCount2 < 0) spaceCount2 = 0; // Prevent negative spaces
            String spaces2 = new String(new char[spaceCount2]).replace('\00', ' ');
            String dateLine = inDt + spaces2 + spaces2 + inTime;

            // ---- Print Strings ----
            printerHelper.printText("" + binding.textHeader1.getText(), AlignStyle.PRINT_STYLE_CENTER);
            printerHelper.printText("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -", AlignStyle.PRINT_STYLE_CENTER);
            printerHelper.printText(receiptLine, AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText(dateLine, AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printText("Vehicle Type : " + binding.textVehicleType.getText(), AlignStyle.PRINT_STYLE_LEFT);
            printerHelper.printLargeText("Vehicle No : " + binding.textVehicleNo.getText().toString(), 30, Paint.Align.CENTER);
            printerHelper.printLargeText("₹ : " + binding.textAmtPerHr.getText(), 30, Paint.Align.CENTER);


            // ---- Print QR Code ----
            binding.textQR.setDrawingCacheEnabled(true);
            Bitmap qrBitmap = Bitmap.createBitmap(binding.textQR.getDrawingCache());
            binding.textQR.setDrawingCacheEnabled(false);
            printerHelper.printQRCode(qrBitmap, AlignStyle.PRINT_STYLE_CENTER,200);
            printerHelper.printText("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -", AlignStyle.PRINT_STYLE_CENTER);

            // ---- Footer ----
            if (binding.textFooter1.length() == 0) {
                printerHelper.printFooter("**** Thank You Visit Again ****");
            } else {
                printerHelper.printFooter(binding.textFooter1.getText().toString());
            }

            // ---- Finish Printing ----
            printerHelper.finishPrint();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PRINT", "print failed: " + e.getMessage());
        }
    }

    @SuppressLint("MissingSuperCall")
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
        Intent dash = new Intent(InTicketGenerationActivity.this, DashboardActivity.class);
        startActivity(dash);
        finish();
    }

}



//        try {
//            int ret;
//            ret = printer.open();
//            if (ret != ErrCode.ERR_SUCCESS) {
//                Log.e("PRINT","open failed" + String.format(" errCode = 0x%x\n", ret));
//                return;
//            }
//
//            ret = printer.startCaching();
//            if (ret != ErrCode.ERR_SUCCESS) {
//                Log.e("PRINT","startCaching failed" + String.format(" errCode = 0x%x\n", ret));
//                return;
//            }
//
//            ret = printer.setGray(3);
//            if (ret != ErrCode.ERR_SUCCESS) {
//                Log.e("PRINT","startCaching failed" + String.format(" errCode = 0x%x\n", ret));
//                return;
//            }
//
//            PrintStatus printStatus = new PrintStatus();
//            ret = printer.getStatus(printStatus);
//            if (ret != ErrCode.ERR_SUCCESS) {
//                Log.e("PRINT","getStatus failed" + String.format(" errCode = 0x%x\n", ret));
//                return;
//            }
//
//            Log.e("PRINT","Temperature = " + printStatus.getmTemperature() + "\n");
//            Log.e("PRINT","Gray = " + printStatus.getmGray() + "\n");
//            if (!printStatus.getmIsHavePaper()) {
//                Log.e("PRINT","Printer out of paper\n");
//                return;
//            }
//
//            Log.e("PRINT","IsHavePaper = true\n");
//
//            // ---- Print Heading ----
//            printer.setAlignStyle(AlignStyle.PRINT_STYLE_CENTER);
//            printer.printStr("PARKING RECEIPT\n\n");
//
//            // ---- Print Ticket Details ----
//            printer.setAlignStyle(AlignStyle.PRINT_STYLE_LEFT);
//            printer.printStr("Receipt No : " + binding.textTicketNo.getText().toString() + "\n");
//            printer.printStr("Date       : " + binding.textDate.getText().toString() + "\n");
//            printer.printStr("Vehicle No : " + binding.textVehicleNo.getText().toString() + "\n");
//            printer.printStr("Vehicle Type: " + binding.textVehicleType.getText().toString() + "\n");
//            printer.printStr("In Time    : " + binding.textIntime.getText().toString() + "\n\n");
//
//            // ---- Print QR Code (Bitmap) ----
//            binding.textQR.setDrawingCacheEnabled(true);
//            Bitmap qrBitmap = Bitmap.createBitmap(binding.textQR.getDrawingCache());
//            binding.textQR.setDrawingCacheEnabled(false);
//
//            if (qrBitmap != null) {
//                ret = printer.printBmp(qrBitmap);
//                if (ret != ErrCode.ERR_SUCCESS) {
//                    Log.e("PRINT","printBmp QR failed" + String.format(" errCode = 0x%x\n", ret));
//                }
//                qrBitmap.recycle();
//            }
//
//            ret = printer.getUsedPaperLenManage();
//            if (ret < 0) {
//                Log.e("PRINT","getUsedPaperLenManage failed" + String.format(" errCode = 0x%x\n", ret));
//            }
//
//            Log.e("PRINT","UsedPaperLenManage = " + ret + "mm \n");
//
//            Bitmap bitmap = Bitmap.createBitmap(384, 400, Bitmap.Config.RGB_565);
//
//            int k_CurX = 0;
//            int k_CurY = 0;
//            int k_TextSize = 24;
//            paint = new Paint();
//            paint.setTextSize(k_TextSize);
//            paint.setColor(Color.BLACK);
//            Canvas canvas = new Canvas(bitmap);
//            bitmap.eraseColor(Color.parseColor("#FFFFFF"));
//
//            Paint.FontMetrics fm = paint.getFontMetrics();
//            int k_LineHeight = (int) ceil(fm.descent - fm.ascent);
//            String displayStr = "IN Receipt";
//            int lineWidth = getTextWidth(displayStr);
//            k_CurX = (384 - lineWidth) / 2;
//            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
//            k_CurY += k_LineHeight + 5;
//            displayStr = "****Thank You Visit Again****";
//            k_CurX = 0;
//            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
//            k_CurY += k_LineHeight;
//            displayStr = "IRCTC Parking";
//            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
//            k_CurY += k_LineHeight;
//
//            displayStr = "Udhana, Surat";
//            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
//            k_CurY += k_LineHeight;
//
//            displayStr = "";
//            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
//            k_CurY += k_LineHeight;
//
//            displayStr = "";
//            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
//            k_CurY += k_LineHeight;
//
//
//            Bitmap newbitmap = Bitmap.createBitmap(bitmap, 0, 0, 384, k_CurY);
//
//            ret = printer.printBmp(newbitmap);
//            if (ret != ErrCode.ERR_SUCCESS) {
//                Log.e("PRINT","printBmp failed" + String.format(" errCode = 0x%x\n", ret));
//                return;
//            }
//
//            if (!bitmap.isRecycled()) {
//                Bitmap mFreeBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
//                canvas.setBitmap(mFreeBitmap);
//                canvas = null;
//                // canvas.drawColor(0, PorterDuff.Mode.CLEAR);
//                bitmap.recycle();
//                bitmap = null;
//                paint.setTypeface(null);
//                paint = null;
//            }
//            if (newbitmap != null && !newbitmap.isRecycled()) {
//                newbitmap.recycle();
//                newbitmap = null;
//            }
//            printer.print(new OnPrinterCallback() {
//                @Override
//                public void onSuccess() {
//                    printer.feed(32);
//                    Log.e("PRINT","print success\n");
//                }
//
//                @Override
//                public void onError(int i) {
//                    Log.e("PRINT","printBmp failed" + String.format(" errCode = 0x%x\n", i));
//                }
//            });
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            Log.e("PRINT","print failed" + e.toString() + "\n");
//        }
//    }
//
//    private static int getTextWidth(String str) {
//        int iRet = 0;
//        if (str != null && str.length() > 0) {
//            int len = str.length();
//            float[] widths = new float[len];
//            paint.getTextWidths(str, widths);
//            for (int j = 0; j < len; j++) {
//                iRet += (int) ceil(widths[j]);
//            }
//        }
//
//        return iRet;
//    }
//    private static Paint paint = null;

