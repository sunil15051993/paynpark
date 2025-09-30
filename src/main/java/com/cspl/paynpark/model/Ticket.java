package com.cspl.paynpark.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tickets")
public class Ticket {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String ticketNo;
    private String vehicleNo;
    private String vehicleType;
    private String durationType;
    private String inTime;
    private String outTime;
    private String date;
    private int paid;
    private int amount;

    // Constructor
    public Ticket(String ticketNo,String date, String vehicleNo, String vehicleType, String durationType, String inTime, String outTime, int paid, int amount) {
        this.ticketNo = ticketNo;
        this.date = date;
        this.vehicleNo = vehicleNo;
        this.vehicleType = vehicleType;
        this.durationType = durationType;
        this.inTime = inTime;
        this.outTime = outTime;
        this.paid = paid;
        this.amount = amount;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getDurationType() {
        return durationType;
    }

    public void setDurationType(String durationType) {
        this.durationType = durationType;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getAmount() {
        return amount;
    }

    public int getPaid() {
        return paid;
    }

    public void setPaid(int paid) {
        this.paid = paid;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
