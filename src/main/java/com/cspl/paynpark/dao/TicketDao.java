package com.cspl.paynpark.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.cspl.paynpark.model.Ticket;

import java.util.List;
@Dao
public interface TicketDao {
    @Insert
    void insert(Ticket ticket);

    @Query("SELECT * FROM tickets")
    List<Ticket> getAllTicket();

    @Query("UPDATE tickets SET outTime = :outTime, amount = :totalPrice WHERE ticketNo = :recpNo")
    void updateTicket(String recpNo, String outTime, int totalPrice);
}
