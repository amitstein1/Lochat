package com.example.stein.client_1;

import android.annotation.SuppressLint;
import android.os.Parcelable;
import android.util.Log;

import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by stein on 12/01/2018.
 */

//there is only one object of User - the others will be from OtherUser class

enum genderType {
    Men, Women;

    public static genderType getGenderByStr(String numstr) {
        if (numstr.equals("0"))
            return genderType.Men;
        else{
            Log.d("user", "gendertype="+genderType.Women.toString());
            return genderType.Women;
        }

    }

}


public class User {
    private static String userName;
    private static String md5Password;
    private static Location userLocation;
    private static String firstName;
    private static  String lastName;
    private static String address;
    private static genderType gender;
    private static int phoneNum;
    private static String email;
    private static String picData;
    private static String lastSeen;
    private static Boolean isConnected; // to the application - is on
    //private Socket userSock;

    public static Map<String, Group> userGroups;
    public static Map<String, Group> hidden_groups;
    public static Map<String, Group> manage_groups;

    public static  Map<String, OtherUser> userFriends;
    public static Map<String, OtherUser> usersWhoAskedRequest;
    public static Map<String, OtherUser> usersWhoWillAnswerRequest;

    public User(String userName, String md5Password, Location userLocation, String firstName, String lastName, String address,
                genderType gender, int phoneNum, String email, String picData, String lastSeen, Boolean isConnected, Map<String, Group> userGroups, Map<String, Group> hiddenGroups,
                Map<String, Group> manageGroups, Map<String, OtherUser> userFriends, Map<String, OtherUser> usersWhoAskedRequest, Map<String, OtherUser> usersWhoWillAnswerRequest) {
        User.userName = userName;
        User.md5Password = md5Password;
        User.userLocation = userLocation;
        User.firstName = firstName;
        User.lastName = lastName;
        User.address = address;
        User.gender = gender;
        User.phoneNum = phoneNum;
        User.email = email;
        User.picData = picData;
        User.lastSeen = lastSeen;
        User.isConnected = isConnected; // to the application - is on
        //this.userSock = userSock;

        User.userGroups = userGroups;
        User.hidden_groups = hiddenGroups;
        User.manage_groups = manageGroups;

        User.userFriends = userFriends;
        User.usersWhoAskedRequest = usersWhoAskedRequest;
        User.usersWhoWillAnswerRequest = usersWhoWillAnswerRequest;


    }

    public static ArrayList<Message> getListOfMessages(String groupId){
        ArrayList<Message> listOfMessages = new ArrayList<Message>(User.userGroups.get(groupId).messages);
        return listOfMessages;
    }


    public static ArrayList<String> getListOfUserGroupsNames(){
        ArrayList<String> listOfNames = new ArrayList<String>();
        for ( Group group : User.userGroups.values()) {
            if (!isExist(group.getName(),listOfNames)){
                //Log.d(TAG, "for some reason must do a log here");
                listOfNames.add(group.getName());
            }
        }
        return listOfNames;
    }

    public static ArrayList<String> getListOfHiddenGroupsNames(){
        ArrayList<String> listOfNames = new ArrayList<String>();
        for ( Group group : User.hidden_groups.values()) {
            if (!isExist(group.getName(),listOfNames)) {
                //Log.d(TAG, "for some reason must do a log here");
                listOfNames.add(group.getName());
            }
        }
        return listOfNames;
    }

    public static ArrayList<String> getListOfManageGroupsNames(){
        ArrayList<String> listOfNames = new ArrayList<String>();
        for ( Group group : User.manage_groups.values()) {
            if (!isExist(group.getName(),listOfNames)) {
                //Log.d(TAG, "for some reason must do a log here");
                listOfNames.add(group.getName());
            }
        }
        return listOfNames;
    }


    public static Boolean isExist(String name1, List<String> listOfNames){
        for (String name2 : listOfNames ) {
            if (name1.equals(name2)) {
                return true;
            }
        }
        return false;
    }
    public static String getUserName() {
        return User.userName;
    }

    public static void setUserName(String userName) {
        User.userName = userName;
    }

    public static String getMd5Password() {
        return md5Password;
    }

    public static void setMd5Password(String md5Password) {
        User.md5Password = md5Password;
    }

    public static Location getUserLocation() {
        return userLocation;
    }

    public static void setUserLocation(Location userLocation) {
        User.userLocation = userLocation;
    }

    public static String getFirstName() {
        return firstName;
    }

    public static void setFirstName(String firstName) {
        User.firstName = firstName;
    }

    public static String getLastName() {
        return lastName;
    }

    public static void setLastName(String lastName) {
        User.lastName = lastName;
    }

    public static String getAddress() {
        return address;
    }

    public static void setAddress(String address) {
        User.address = address;
    }

    public static genderType getGender() {
        return gender;
    }

    public static void setGender(genderType gender) {
        User.gender = gender;
    }

    public static int getPhoneNum() {
        return phoneNum;
    }

    public static void setPhoneNum(int phoneNum) {
        User.phoneNum = phoneNum;
    }

    public static String getEmail() {
        return email;
    }

    public static void setEmail(String email) {
        User.email = email;
    }

    public static String getPicData() {
        return picData;
    }

    public static void setPicData(String picData) {
        User.picData = picData;
    }

    public static Boolean getIsConnected() {
        return isConnected;
    }

    public static void setIsconnected(Boolean isConnected) {
        User.isConnected = isConnected;
    }

    public static String getLastSeen() {
        return lastSeen;
    }


    @SuppressLint("SimpleDateFormat")
    public static void setLastSeen() {
        User.lastSeen =  new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());

    }
}
