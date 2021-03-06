package com.gmail.rami.abushaqra79.sharerent;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Model class for the user.
 */
@IgnoreExtraProperties
public class User {

    // Fields
    private String email;
    private String name;
    private String phoneNumber;
    private String location;
    private String profilePictureUrl;
    private String token;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String name, String phoneNumber, String location,
                String profilePictureUrl, String token) {
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.location = location;
        this.profilePictureUrl = profilePictureUrl;
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLocation() {
        return location;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public String getToken() {
        return token;
    }
}
