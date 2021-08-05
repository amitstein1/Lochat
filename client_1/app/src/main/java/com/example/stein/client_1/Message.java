package com.example.stein.client_1;

import android.util.Log;

import java.util.Date;

/**
 * Created by stein on 12/01/2018.
 */
enum msgType {
    Private, Public;

    public static msgType getMsgTypeByStr(String numStr) {
        if (numStr.equals("0"))
            return msgType.Private;
        else {
            return msgType.Public;
        }
    }
}


public class Message {
    private static final String TAG = "Message";
    private String id;
    private msgType type;
    private String srcUserId;
    private String groupId;
    private String dstId;
    private String sendTime;
    private String data;

    public Message(String id, msgType type, String groupId, String srcUserId, String dstId, String sendTime, String data) {
        this.id = id;
        this.srcUserId = srcUserId;
        this.type = type;
        this.groupId = groupId;
        this.dstId = dstId;
        this.sendTime = sendTime;//we will get the time before creating this message obj, then convert to string and then insert as a parameter in this constractor
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public String getSrcUserId() {
        return srcUserId;
    }

    public String getDstId() {
        return dstId;
    }

    public String getSendTime() {
        return sendTime;
    }

    public String getData() {
        return data;
    }

    public String getGroupId() {
        return groupId;
    }

    public msgType getType() {
        return type;
    }

    public void setType(msgType type) {
        this.type = type;
    }


    @Override
    public String toString() {
        return this.id + "#" + this.srcUserId + "#" + srcUserId + "#" + this.type + "#" + this.groupId + "#" + this.dstId + "#" + this.sendTime + "#" + this.data;//we will get the time before creating this message obj, then convert to string and then insert as a parameter in this constractor

    }

    @Override
    public boolean equals(Object other) {
        /*if (other == null || other == this || !(other instanceof Message))
            return false;

        Message otherMessage = (Message) other;
        Log.d(TAG, "equals before comparing each element");
        if (!otherMessage.id.equals(this.id)) {
            Log.d(TAG, "id:" + this.id + "=" + otherMessage.id);
            return false;
        }
        if (!otherMessage.srcUserId.equals(this.srcUserId)) {
            Log.d(TAG, "srcUserId:" + this.srcUserId + "=" + otherMessage.srcUserId);
            return false;
        }
        if (!otherMessage.type.equals(this.type)) {
            Log.d(TAG, "type:" + this.type + "=" + otherMessage.type);
            return false;
        }
        if (!otherMessage.groupId.equals(this.groupId)) {
            Log.d(TAG, "groupId:" + this.groupId + "=" + otherMessage.groupId);
            return false;
        }
        if (!otherMessage.dstId.equals(this.dstId)) {
            Log.d(TAG, "dstId:" + this.dstId + "=" + otherMessage.dstId);
            return false;
        }
        if (!otherMessage.sendTime.equals(this.sendTime)) {
            Log.d(TAG, "sendTime:" + this.sendTime + "=" + otherMessage.sendTime);
            return false;
        }
        if (!otherMessage.data.equals(this.data)) {
            Log.d(TAG, "data:" + this.data + "=" + otherMessage.data);
            return false;
        }
        Log.d(TAG, "equals = true");
        return true;*/
        return ((Message)other).toString().equals(this.toString());
    }
}

