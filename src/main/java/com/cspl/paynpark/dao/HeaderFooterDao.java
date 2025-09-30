package com.cspl.paynpark.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.cspl.paynpark.model.HeaderFooter;
import com.cspl.paynpark.model.VehicType;

import java.util.List;

@Dao
public interface HeaderFooterDao {
    @Insert
    void insert(HeaderFooter headerFooter);

    @Query("SELECT * FROM header LIMIT 1")
    HeaderFooter getHeaderFooter();

}
