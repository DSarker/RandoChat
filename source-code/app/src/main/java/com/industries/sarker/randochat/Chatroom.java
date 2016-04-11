package com.industries.sarker.randochat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by David on 3/28/16.
 */
public class Chatroom implements Serializable {
    private String key;
    private HashMap<String, Message> messages;
    private String numOfUsers;
    private String user1;
    private String user2;

    public Chatroom() {

    }


    public Map<String, Message> getMessages() {
        return messages;
    }

    public void setMessages(HashMap<String, Message> messages) {
        this.messages = messages;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    public String getNumOfUsers() {
        return numOfUsers;
    }

    public void setNumOfUsers(String numOfUsers) {
        this.numOfUsers = numOfUsers;
    }

    public Chatroom(HashMap<String, Message> messages, String numOfUsers, String user1, String user2) {
        this.messages = messages;
        this.numOfUsers = numOfUsers;
        this.user1 = user1;
        this.user2 = user2;
    }
}
