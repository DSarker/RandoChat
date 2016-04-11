package com.industries.sarker.randochat;

import java.io.Serializable;

/**
 * Created by David on 3/25/16.
 */
public class Message implements Serializable{
    private String author;
    private String text;
    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Message() {

    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Message(String author, String text, String time) {

        this.author = author;
        this.text = text;
        this.time = time;
    }
}
