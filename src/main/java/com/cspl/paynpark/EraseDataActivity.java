package com.cspl.paynpark;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.cspl.paynpark.databinding.ActivityEraseDataBinding;
import com.cspl.paynpark.databinding.ActivityFindBinding;
import com.cspl.paynpark.dbhelper.AppDatabase;

public class EraseDataActivity extends AppCompatActivity {
    private ActivityEraseDataBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEraseDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initView();

    }

    private void initView() {
        binding.buttonErase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(() -> {
                    AppDatabase db = AppDatabase.getInstance(EraseDataActivity.this);

                    db.ticketDao().deleteAllTickets();
                }).start();

                Toast.makeText(EraseDataActivity.this, "All tickets deleted", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
