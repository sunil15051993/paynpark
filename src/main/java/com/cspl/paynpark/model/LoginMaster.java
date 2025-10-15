package com.cspl.paynpark.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "loginmaster",
        indices = {@Index(value = {"userId"}, unique = true)})
public class LoginMaster {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String userId;
    private String userPwd;
    private String role;
    private String serial;
    private String userStartDt;
    private String userEndDt;

    public LoginMaster(String userId, String userPwd, String role, String serial, String userStartDt, String userEndDt) {
        this.userId = userId;
        this.userPwd = userPwd;
        this.role = role;
        this.serial = serial;
        this.userStartDt = userStartDt;
        this.userEndDt = userEndDt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getUserStartDt() {
        return userStartDt;
    }

    public void setUserStartDt(String userStartDt) {
        this.userStartDt = userStartDt;
    }

    public String getUserEndDt() {
        return userEndDt;
    }

    public void setUserEndDt(String userEndDt) {
        this.userEndDt = userEndDt;
    }
}
