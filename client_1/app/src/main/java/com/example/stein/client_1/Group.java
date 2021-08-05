package com.example.stein.client_1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by stein on 12/01/2018.
 */

enum groupType {
    Static, Dynamic;
    public static groupType getGroupTypeByStr(String numStr) {
        if (numStr.equals("0"))
            return groupType.Static;
        else{
            return groupType.Dynamic;
        }
    }
}

public class Group {
    private String id;
    private groupType type;
    private Location midLoc;
    private double radius;
    private String name;
    private String picData;
    private String managerId; //if the client want to check the user location or other props he can look at the group users with the id key of the manager
    private String timeCreated;

    final int TimeToRemoveMsgs = 24;

    public List<Message> messages;
    public Map<String, OtherUser> users; //user id,user object

    Group(String id, groupType type, Location midLoc, double radius, String name, String picData,
          String managerId, String timeCreated, List<Message> messages, Map<String, OtherUser> users) {
        this.id = id;
        this.type = type;
        this.midLoc = midLoc;
        this.radius = radius;
        this.name = name;
        this.picData = picData;
        this.managerId = managerId;
        this.timeCreated = timeCreated;

        this.messages = messages;//new ArrayList<Message>();
        this.users = users;//new HashMap<String, OtherUser>(); //user id,user object
    }

    public String getId() {
        return id;
    }

    public groupType getType() {
        return type;
    }

    public Location getMidLoc() {
        return midLoc;
    }

    public void setMidLoc(Location midLoc) {
        this.midLoc = midLoc;
    }

    public double getRadius() {
        return radius;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicData() {
        return picData;
    }

    public void setPicData(String picData) {
        this.picData = picData;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getTimeCreated() {
        return timeCreated;
    }
}
