package com.cspl.paynpark.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.cspl.paynpark.model.StatusReport;
import com.cspl.paynpark.model.Ticket;
import com.cspl.paynpark.model.TicketReport;
import com.cspl.paynpark.model.TotalColReport;

import java.util.List;
@Dao
public interface TicketDao {
    @Insert
    void insert(Ticket ticket);

    @Query("SELECT * FROM tickets")
    List<Ticket> getAllTicket();

    @Query("UPDATE tickets SET outTime = :outTime, amount = :totalPrice WHERE ticketNo = :recpNo AND date = :inDate")
    void updateTicket(String recpNo, String inDate, String outTime, int totalPrice);

    @Query("SELECT * FROM tickets WHERE vehicleNo = :vehicleNo AND date = :date LIMIT 1")
    Ticket getTicketByVehicleAndDate(String vehicleNo, String date);

    @Query("SELECT vehicleType, COUNT(*) as count, SUM(amount) as totalAmt " +
            "FROM tickets WHERE date = :date AND log = :emp GROUP BY vehicleType")
    List<TicketReport> getReportByDate(String date, String emp);

    @Query("SELECT vehicleType, " +
            "SUM(CASE WHEN inTime IS NOT NULL AND (outTime IS NULL OR outTime = '') THEN 1 ELSE 0 END) AS inCount, " +
            "SUM(CASE WHEN inTime IS NOT NULL AND outTime IS NOT NULL AND outTime != '' THEN 1 ELSE 0 END) AS outCount, " +
            "SUM(amount) as totalAmt " +
            "FROM tickets " +
            "WHERE date = :date " +
            "GROUP BY vehicleType")
    List<StatusReport> getStatusByDate(String date);
    @Query("SELECT COUNT(vehicleNo) as totalVehicles, SUM(amount) as totalCollection FROM tickets")
    TotalColReport getTotalData();

    @Query("DELETE FROM tickets")
    void deleteAllTickets();
}
