package com.gmail.rami.abushaqra79.sharerent;

import android.text.TextUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ReadAndWriteDatabase {

    private final String DATABASE_URL_LOCATION
            = "https://share-rent-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseUser user;

    public ReadAndWriteDatabase() {
        database = FirebaseDatabase.getInstance(DATABASE_URL_LOCATION);
        databaseReference = database.getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    public void readDataForUser(String userId, ValueEventListener listener) {
        if (!TextUtils.isEmpty(userId)) {
            databaseReference.child("Users").child(userId)
                    .addListenerForSingleValueEvent(listener);
        }
    }

    public void saveUserInfoToDatabase(String userId, String dbRef, String updatedInfo) {
        if (!TextUtils.isEmpty(userId)) {
            if (dbRef.equals("email")) {
                databaseReference.child("Users").child(userId).child(dbRef).setValue(updatedInfo);
            } else {
                databaseReference.child("Users").child(userId).child("user-info").child(dbRef).setValue(updatedInfo);
            }
        }
    }
}
