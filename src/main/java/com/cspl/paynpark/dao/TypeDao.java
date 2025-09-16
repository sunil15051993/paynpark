package com.cspl.paynpark.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.cspl.paynpark.model.VehicType;

import java.util.List;

@Dao
public interface TypeDao {
    @Insert
    void insert(VehicType type);

    @Query("SELECT * FROM vehtypes")
    LiveData<List<VehicType>> getAllTypes();

    @Query("UPDATE tickets SET outTime = :outTime, amount = :totalPrice WHERE ticketNo = :recpNo")
    void updateTicket(String recpNo, String outTime, int totalPrice);
}
