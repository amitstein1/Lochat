package com.example.stein.client_1;

import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by stein on 18/01/2018.
 */


public class Serialization {
    private static final String TAG = "Serialization";
    public Serialization() {

    }

    public User getNewUser(List<String> params) {
        Log.d(TAG, "params:" + String.valueOf(params));
        String userName = params.get(0);
        String md5Password = params.get(1);
        Location userLocation = getLocationbyStr(params.get(2));
        String firstName = params.get(3);
        String lastName = params.get(4);
        String address = params.get(5);
        genderType gender = genderType.getGenderByStr(params.get(6));
        int phoneNum = Integer.parseInt(params.get(7));
        String email = params.get(8);
        String picData = params.get(9);
        String lastSeen = params.get(10);
        Boolean isConnected = Boolean.parseBoolean(params.get(11).toLowerCase());
        String userGroups_str = params.get(12);
        Map<String, Group> userGroups = getGroupsListByStr(userGroups_str);
        String hiddenGroups_str = params.get(13);
        Map<String, Group> hiddenGroups = getGroupsListByStr(hiddenGroups_str);
        String manage_groups_str = params.get(14);
        Map<String, Group> manage_groups = getGroupsListByStr(manage_groups_str);


        Map<String, OtherUser> userFriends = new HashMap<String, OtherUser>();
        Map<String, OtherUser> usersWhoAskedRequest = new HashMap<String, OtherUser>();
        Map<String, OtherUser> usersWhoWillAnswerRequest = new HashMap<String, OtherUser>();

        if (params.size() == 18) {//it means at least one of the 3 last parameters have a value so 3
            // of them are included in the params list but it is possible that each one of them
            // is null so we created before empty lists for a case where the params sive is 15.
            String userFriends_str = params.get(15);
            userFriends = getOtherUsersListByStr(userFriends_str);
            String usersWhoAskedRequest_str = params.get(16);
            usersWhoAskedRequest = getOtherUsersListByStr(usersWhoAskedRequest_str);
            String usersWhoWillAnswerRequest_str = params.get(17);
            usersWhoWillAnswerRequest = getOtherUsersListByStr(usersWhoWillAnswerRequest_str);
        }


        User user = new User(userName, md5Password, userLocation, firstName, lastName, address, gender, phoneNum, email, picData, lastSeen,
                isConnected, userGroups, hiddenGroups, manage_groups, userFriends, usersWhoAskedRequest, usersWhoWillAnswerRequest);
        return user;
    }

    public synchronized void updatedStaticUser(List<String> params){

        Log.d(TAG, "params:" + String.valueOf(params));
        User.setUserName(params.get(0));
        User.setMd5Password(params.get(1));
        User.setUserLocation(getLocationbyStr(params.get(2)));
        User.setFirstName(params.get(3));
        User.setLastName(params.get(4));
        User.setAddress(params.get(5));
        User.setGender(genderType.getGenderByStr(params.get(6)));
        User.setPhoneNum(Integer.parseInt(params.get(7)));
        User.setEmail(params.get(8));
        User.setPicData(params.get(9));
        User.setLastSeen();//do not need the server to updata last seen...
        User.setIsconnected(Boolean.parseBoolean(params.get(11).toLowerCase()));
        String userGroups_str = params.get(12);
        Log.d(TAG, "userGroups_str:" + userGroups_str);
        Map<String, Group> new_userGroups = getGroupsListByStr(userGroups_str);
        User.userGroups =  new_userGroups;
        Log.d(TAG, "User.userGroups returned = " + User.userGroups.toString());
        String hiddenGroups_str = params.get(13);
        Map<String, Group> new_hiddenGroups = getGroupsListByStr(hiddenGroups_str);
        User.hidden_groups =  new_hiddenGroups;
        String manage_groups_str = params.get(14);
        Map<String, Group> new_manage_groups = getGroupsListByStr(manage_groups_str);
        User.manage_groups =  new_manage_groups;


        Map<String, OtherUser> new_userFriends = new HashMap<String, OtherUser>();
        Map<String, OtherUser> new_usersWhoAskedRequest = new HashMap<String, OtherUser>();
        Map<String, OtherUser> new_usersWhoWillAnswerRequest = new HashMap<String, OtherUser>();

        if (params.size() == 18) {//it means at least one of the 3 last parameters have a value so 3
            // of them are included in the params list but it is possible that each one of them
            // is null so we created before empty lists for a case where the params sive is 15.
            String userFriends_str = params.get(15);
            new_userFriends = getOtherUsersListByStr(userFriends_str);
            User.userFriends = new_userFriends;
            String usersWhoAskedRequest_str = params.get(16);
            new_usersWhoAskedRequest = getOtherUsersListByStr(usersWhoAskedRequest_str);
            User.usersWhoAskedRequest = new_usersWhoAskedRequest;
            String usersWhoWillAnswerRequest_str = params.get(17);
            new_usersWhoWillAnswerRequest = getOtherUsersListByStr(usersWhoWillAnswerRequest_str);
            User.usersWhoWillAnswerRequest = new_usersWhoWillAnswerRequest;
        }
        //Log.d(TAG, "########################new group name:" + new_userGroups.get("0").getName());
        //Log.d(TAG, "########################new group name:" + User.userGroups.get("0").getName());
    }


    public Map<String, Group> getGroupsListByStr(String strGroups) {
        //Log.d(TAG, "strGroups with '[]':" + strGroups);
        strGroups = strGroups.substring(1, strGroups.length() - 1);//to remove : []
        Log.d(TAG, "strGroups :" + strGroups);
        /*if (strGroups.equals("")){
            strGroups = ",";
        }*/
        List<String> listStrGroups = Arrays.asList(strGroups.split(", "));//maybe need \s*, \s*
        if (listStrGroups.size()==2){
            Log.d(TAG, "groups 1:" + listStrGroups.get(0));
            Log.d(TAG, "groups 2:" + listStrGroups.get(1));
        }
        Map<String, Group> dictGroups = new HashMap<String, Group>();
        //Log.d(TAG, "listStrGroups size:" + String.valueOf(listStrGroups.size()));
        if (!strGroups.equals("")) {// > at leas 1 because there is one cell which is: ""
            for (String str_group : listStrGroups) {
                Log.d(TAG, "group:" + str_group);
                Group newGroup = getGroupByStr(str_group);
                dictGroups.put(newGroup.getId(), newGroup);
            }
        }
        return dictGroups;
    }


    public Group getGroupByStr(String strGroup) {
        Log.d(TAG, "getGroupByStr strGroup: " + strGroup);
        List<String> listStrGroupProps = Arrays.asList(strGroup.split("\\s*&\\s*"));
        String id = listStrGroupProps.get(0);
        groupType type = groupType.getGroupTypeByStr(listStrGroupProps.get(1));
        Location location = getLocationbyStr(listStrGroupProps.get(2));
        double radius = Double.parseDouble(listStrGroupProps.get(3));
        String name = listStrGroupProps.get(4);
        String picData = listStrGroupProps.get(5);
        String managerId = listStrGroupProps.get(6);
        String timeCreated = listStrGroupProps.get(7);
        String time_to_remove_msg = listStrGroupProps.get(8);

        String users_str = listStrGroupProps.get(9);
        Map<String, OtherUser> users = new HashMap<String, OtherUser>();
        if (!users_str.equals("-")) {
            Log.d(TAG, "users_str not equal\"-\"");
            users = getOtherUsersListByStr(users_str);
        }

        String messages_str = listStrGroupProps.get(10);
        List<Message> messages = new ArrayList<Message>();
        if (!messages_str.equals("-")) {
            Log.d(TAG, "messages_str not equal\"-\"");
            messages = getMessagesByStr(messages_str);
        }

        Group newGroup = new Group(id, type, location, radius, name, picData, managerId, timeCreated, messages, users);
        return newGroup;
    }

    //#
    //make a function in the server that will seperate rigte the messages messages list with '!'
    public List<Message> getMessagesByStr(String strMessages) {
        //create a class in the server and make the split sign : #
        List<String> listStrMessages = Arrays.asList(strMessages.split("\\s*!\\s*"));
        List<Message> listMessages = new ArrayList<Message>();
        Log.d(TAG, "getMessagesByStr");
        for (String str_Message : listStrMessages) {
            Log.d(TAG, "in loop:"+str_Message);
            Message newMessage = getMessageByStr(str_Message);
            listMessages.add(newMessage);
        }
        Log.d(TAG, "after getMessagesByStr");
        return listMessages;
    }

    //put the right values in the newMessage object and check and make a function in the server that will seperate right the messages properties with '!'
    public Message getMessageByStr(String strMessage) {
        Log.d(TAG, "before getMessageByStr");
        List<String> listStrstrMessageProps = Arrays.asList(strMessage.split("\\s*#\\s*"));
        Log.d(TAG, "0");
        String msgId = listStrstrMessageProps.get(0);
        Log.d(TAG, "1");
        msgType type = msgType.getMsgTypeByStr(listStrstrMessageProps.get(1));
        Log.d(TAG, "2");
        String group_id = listStrstrMessageProps.get(2);
        Log.d(TAG, "3");
        String srcUserId = listStrstrMessageProps.get(3);
        Log.d(TAG, "4");
        String dstId = listStrstrMessageProps.get(4);
        Log.d(TAG, "5");
        String sendTime = listStrstrMessageProps.get(5);
        Log.d(TAG, "6");
        String data = listStrstrMessageProps.get(6);
        Log.d(TAG, "7");
        //String id, msgType type, String groupId, String srcUserId, String dstId, String sendTime, String data
        Message newMessage = new Message(msgId, type, group_id, srcUserId, dstId, sendTime, data);
        Log.d(TAG, "after getMessageByStr");
        return newMessage;
    }


    //make a function in the server that will seperate rigte the OtherUsers list with '!'
    public Map<String, OtherUser> getOtherUsersListByStr(String strOtherUsers) {
        Log.d(TAG, strOtherUsers);
        List<String> listStrOtherUsers = Arrays.asList(strOtherUsers.split("\\s*!\\s*"));
        Log.d(TAG, "split(!):" + String.valueOf(listStrOtherUsers));
        Map<String, OtherUser> listOtherUsers = new HashMap<String, OtherUser>();
        if (listStrOtherUsers.size() > 1) {// > 1 because there is one cell which is: ""
            Log.d(TAG, "not supposed");
            for (String str_OtherUser : listStrOtherUsers) {
                OtherUser newOtherUser = getOtherUserByStr(str_OtherUser);
                listOtherUsers.put(newOtherUser.getUserName(), newOtherUser);
            }
        }
        return listOtherUsers;
    }

    //put the right values in the OtherUser object and check and make a function in the server that will seperate right the OtherUser properties with '!'
    public OtherUser getOtherUserByStr(String strOtherUser) {
        //strOtherUser = strOtherUser.substring(1,strOtherUser.length()-1);
        List<String> listStrOtherUserProps = Arrays.asList(strOtherUser.split("\\s*#\\s*"));
        String userName = listStrOtherUserProps.get(0);
        String md5Password = listStrOtherUserProps.get(1);
        Location userLocation = getLocationbyStr(listStrOtherUserProps.get(2));
        String firstName = listStrOtherUserProps.get(3);
        String lastName = listStrOtherUserProps.get(4);
        String address = listStrOtherUserProps.get(5);
        //genderType gender = genderType.getGender(listStrOtherUserProps.get(6));
        genderType gender = genderType.getGenderByStr(listStrOtherUserProps.get(6));
        int phoneNum = Integer.parseInt(listStrOtherUserProps.get(7));
        String email = listStrOtherUserProps.get(8);
        String picData = listStrOtherUserProps.get(9);
        String lastSeen = listStrOtherUserProps.get(10);
        Boolean isConnected = Boolean.parseBoolean(listStrOtherUserProps.get(11).toLowerCase());


        OtherUser newOtherUser = new OtherUser(userName, md5Password, userLocation, firstName, lastName, address, gender, phoneNum, email, picData, lastSeen,
                isConnected);
        return newOtherUser;
    }


    public Location getLocationbyStr(String userLocation_str) {
        // check how the server sends the location - just convert the string to double
        String[] arrLoc = userLocation_str.split("\\|"); // "\\|" because "|" is a pipe character
        Log.d(TAG, "after conver location: latitude:" + arrLoc[0] + ",longitude: " + arrLoc[1]);
        double latitude = Double.parseDouble(arrLoc[0]);
        double longitude = Double.parseDouble(arrLoc[1]);
        Location newLoc = new Location(latitude, longitude);
        return newLoc;
    }
}
