package com.cspl.paynpark.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cspl.paynpark.model.LoginMaster;
import com.cspl.paynpark.model.Ticket;
import com.cspl.paynpark.model.VehicFare;
import com.cspl.paynpark.model.VehicType;

import java.util.List;

@Dao
public interface LoginDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(LoginMaster loginMaster);

    @Query("SELECT * FROM loginmaster")
    LiveData<List<LoginMaster>> getAllUser();

    @Query("SELECT * FROM loginmaster WHERE userId = :email AND userPwd = :password AND serial = :serial LIMIT 1")
    LoginMaster login(String email, String password, String serial);

}
