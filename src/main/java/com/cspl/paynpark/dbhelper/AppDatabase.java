package com.cspl.paynpark.dbhelper;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.cspl.paynpark.dao.TicketDao;
import com.cspl.paynpark.model.Ticket;

@Database(entities = {Ticket.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract TicketDao ticketDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "ticket_db")
                    .allowMainThreadQueries() // not recommended for production
                    .build();
        }
        return instance;
    }
}
