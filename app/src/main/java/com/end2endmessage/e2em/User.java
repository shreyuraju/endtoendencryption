package com.end2endmessage.e2em;

public class User {
    int UID;
    String email;

    public User(int UID, String email) {
        this.UID = UID;
        this.email = email;
    }

    public int getUID() {
        return UID;
    }

    public void setUID(int UID) {
        this.UID = UID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}