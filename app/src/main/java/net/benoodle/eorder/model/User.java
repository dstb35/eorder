package net.benoodle.eorder.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("mail")
    private String mail;

    @SerializedName("name")
    private String name;

    public User(String mail, String name){
        this.mail = mail;
        this.name = name;
    }
}
