package com.cspl.paynpark.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vehtypes")
public class VehicType {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String vehicleType;


    // Constructor
    public VehicType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getVehicleType() {
        return vehicleType;
    }
    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
}
