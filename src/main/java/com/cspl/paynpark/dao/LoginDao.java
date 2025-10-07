package com.cspl.paynpark.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.cspl.paynpark.model.LoginMaster;
import com.cspl.paynpark.model.Ticket;
import com.cspl.paynpark.model.VehicFare;

import java.util.List;

@Dao
public interface LoginDao {
    @Insert
    void insert(LoginMaster loginMaster);

    @Query("SELECT * FROM loginmaster WHERE userId = :email AND userPwd = :password AND serial = :serial LIMIT 1")
    LoginMaster login(String email, String password, String serial);

}
