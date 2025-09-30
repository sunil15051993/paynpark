package com.cspl.paynpark.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.cspl.paynpark.model.VehicFare;
import com.cspl.paynpark.model.VehicType;

import java.util.List;

@Dao
public interface FareDao {
    @Insert
    void insert(VehicFare fare);

    @Query("SELECT * FROM vehfare WHERE vehicleType = :vehicleType AND hours = :hours LIMIT 1")
    VehicFare getFareFor(String vehicleType, int hours);

    @Query("SELECT * FROM vehfare")
    List<VehicFare> getAllFare();

    @Query("SELECT * FROM vehfare WHERE vehicleType = :vehicleType AND hours = :hours LIMIT 1")
    VehicFare getFarePrice(String vehicleType, int hours);

}
