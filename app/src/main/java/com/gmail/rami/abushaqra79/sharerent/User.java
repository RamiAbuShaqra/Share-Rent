package com.gmail.rami.abushaqra79.sharerent;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String email;
    public String password;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}