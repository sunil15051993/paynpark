package com.cspl.paynpark.dbhelper;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.cspl.paynpark.dao.FareDao;
import com.cspl.paynpark.dao.TicketDao;
import com.cspl.paynpark.dao.TypeDao;
import com.cspl.paynpark.model.Ticket;
import com.cspl.paynpark.model.VehicFare;
import com.cspl.paynpark.model.VehicType;

@Database(entities = {Ticket.class, VehicType.class, VehicFare.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract TicketDao ticketDao();
    public abstract TypeDao typeDao();
    public abstract FareDao fareDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "ticket_db")
                    .allowMainThreadQueries() // ⚠️ Only for dev/testing
                    .fallbackToDestructiveMigration() // recreate db if schema changes
                    .build();
        }
        return instance;
    }
}