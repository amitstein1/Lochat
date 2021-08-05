package com.example.stein.client_1;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Created by stein on 16/01/2018.
 */

//a short object for user - the same but without the lists - for all the other users

public class OtherUser {
    private String userName;
    private String md5Password;
    private Location userLocation;
    private String firstName;
    private String lastName;
    private String address;
    private genderType gender;
    private int phoneNum;
    private String email;
    private String picData;
    private String lastSeen;
    private Boolean isConnected; // to the application - is on
    //private Socket userSock;

    public OtherUser(String userName, String md5Password, Location userLocation, String firstName,
                  String lastName, String address, genderType gender, int phoneNum,
                  String email, String picData, String lastSeen, Boolean isConnected){
        this.userName = userName;
        this.md5Password = md5Password;
        this.userLocation = userLocation;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.gender = gender;
        this.phoneNum = phoneNum;
        this.email = email;
        this.picData = picData;
        this.lastSeen = lastSeen;
        this.isConnected = isConnected; // to the application - is on
        //this.userSock = userSock;

    }
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMd5Password() {
        return md5Password;
    }

    public void setMd5Password(String md5Password) {
        this.md5Password = md5Password;
    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public genderType getGender() {
        return gender;
    }

    public void setGender(genderType gender) {
        this.gender = gender;
    }

    public int getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(int phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPicData() {
        return picData;
    }

    public void setPicData(String picData) {
        this.picData = picData;
    }

    public Boolean getIsConnected() {
        return isConnected;
    }

    public void setIs_connected(Boolean isConnected) {
        this.isConnected = isConnected;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen() {
        Date currentTime = Calendar.getInstance().getTime();
        this.lastSeen = currentTime.toString();
    }

}
