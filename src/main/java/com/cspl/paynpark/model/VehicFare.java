package com.cspl.paynpark.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vehfare")
public class VehicFare {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String vehicleType;
    private long hours;
    private int price;


    // Constructor
    public VehicFare(String vehicleType, long hours, int price) {
        this.vehicleType = vehicleType;
        this.hours = hours;
        this.price = price;
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

    public long getHours() {
        return hours;
    }

    public void setHours(long hours) {
        this.hours = hours;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
