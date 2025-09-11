package com.cspl.paynpark;

import static com.ftpos.library.smartpos.errcode.ErrCode.ERR_SUCCESS;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cspl.paynpark.databinding.ActivityMainBinding;
import com.ftpos.library.smartpos.buzzer.Buzzer;
import com.ftpos.library.smartpos.crypto.Crypto;
import com.ftpos.library.smartpos.device.Device;
import com.ftpos.library.smartpos.emv.Emv;
import com.ftpos.library.smartpos.icreader.IcReader;
import com.ftpos.library.smartpos.keymanager.KeyManager;
import com.ftpos.library.smartpos.led.Led;
import com.ftpos.library.smartpos.magreader.MagReader;
import com.ftpos.library.smartpos.memoryreader.MemoryReader;
import com.ftpos.library.smartpos.nfcreader.NfcReader;
import com.ftpos.library.smartpos.printer.Printer;
import com.ftpos.library.smartpos.psamreader.PsamReader;
import com.ftpos.library.smartpos.serialport.SerialPort;
import com.ftpos.library.smartpos.servicemanager.OnServiceConnectCallback;
import com.ftpos.library.smartpos.servicemanager.ServiceManager;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    public static Printer printer = null;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
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

        init();

    }

    private void init(){
        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dash = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(dash);
            }
        });
    }
}