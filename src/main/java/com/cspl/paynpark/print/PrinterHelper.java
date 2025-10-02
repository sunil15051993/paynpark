package com.cspl.paynpark.print;

import static java.lang.Math.ceil;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.printer.OnPrinterCallback;
import com.ftpos.library.smartpos.printer.Printer;

public class PrinterHelper {

    private static Paint paint = new Paint();
    private final Printer printer;

    public PrinterHelper(Printer printer) {
        this.printer = printer;
        paint.setTextSize(24);
        paint.setColor(Color.BLACK);
    }

    public void printLargeText(String text, int fontSize) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        paint.setColor(Color.BLACK);

        // Bold text
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setFakeBoldText(true);

        // Calculate exact height
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        int height = (int) (fontMetrics.bottom - fontMetrics.top);
        int width = 360; // printer max width in pixels

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        // Draw text at baseline
        canvas.drawText(text, 0, -fontMetrics.top, paint);

        printer.printBmp(bitmap);
        printer.feed(10);
        bitmap.recycle();
    }

    /**
     * Print only text content
     */
    public void printText(String text, int alignStyle) {
        try {
            int ret = printer.setAlignStyle(alignStyle);
            if (ret == ErrCode.ERR_SUCCESS) {
                printer.printStr(text + "\n");
            } else {
                Log.e("PrinterHelper", "setAlignStyle failed err=0x" + ret);
            }
        } catch (Exception e) {
            Log.e("PrinterHelper", "printText Exception: " + e.getMessage());
        }
    }

    /**
     * Print a QR code bitmap
     */
    public void printQRCode(Bitmap qrBitmap, int alignStyle) {
        if (qrBitmap == null) return;
        try {
            int ret = printer.setAlignStyle(alignStyle);
            if (ret == ErrCode.ERR_SUCCESS) {
                printer.printBmp(qrBitmap);
            }else {
                Log.e("PrinterHelper", "printBmp QR failed err=0x" + ret);
            }
        } catch (Exception e) {
            Log.e("PrinterHelper", "printQRCode Exception: " + e.getMessage());
        } finally {
            qrBitmap.recycle();
        }
    }

    /**
     * Print footer message with custom Canvas (example)
     */
    public void printFooter(String footerText) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(384, 400, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            bitmap.eraseColor(Color.WHITE);

            int curY = 40;

            // Split footer text by new lines
            String[] lines = footerText.split("\n");

            for (String line : lines) {
                if (line.trim().isEmpty()) continue; // skip empty lines

                int textWidth = getTextWidth(line);
                int startX = (384 - textWidth) / 2; // center align
                canvas.drawText(line, startX, curY, paint);

                curY += paint.getTextSize();   // Use text size as baseline spacing (no extra gap)
            }

            Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, 384, curY);
            int ret = printer.printBmp(newBitmap);

            if (ret != ErrCode.ERR_SUCCESS) {
                Log.e("PrinterHelper", "printFooter failed err=0x" + ret);
            }

            bitmap.recycle();
            newBitmap.recycle();
        } catch (Exception e) {
            Log.e("PrinterHelper", "printFooter Exception: " + e.getMessage());
        }
    }

    private int getTextWidth(String str) {
        if (str == null) return 0;
        float[] widths = new float[str.length()];
        paint.getTextWidths(str, widths);
        int width = 0;
        for (float w : widths) width += ceil(w);
        return width;
    }


    /**
     * Finalize and send data to printer
     */
    public void finishPrint() {
        printer.print(new OnPrinterCallback() {
            @Override
            public void onSuccess() {
                printer.feed(32);
                Log.e("PrinterHelper", "Print Success");
            }

            @Override
            public void onError(int code) {
                Log.e("PrinterHelper", "Print Error err=0x" + code);
            }
        });
    }
}
