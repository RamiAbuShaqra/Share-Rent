package com.gmail.rami.abushaqra79.sharerent;

import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ReadAndWriteDatabase {

    private final String DATABASE_URL_LOCATION
            = "https://share-rent-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private Uri downloadUri;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    public ReadAndWriteDatabase() {
        database = FirebaseDatabase.getInstance(DATABASE_URL_LOCATION);
        databaseReference = database.getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
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

    public void saveDataToStorage(String userId, Uri imageUri) {
        String path = "pictures/user_profile_picture.png";
        StorageReference ref = storageReference.child(path);

        UploadTask uploadTask = ref.putFile(imageUri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return ref.getDownloadUrl();
            }
        });

        urlTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadUri = task.getResult();

                    databaseReference.child("Users").child(userId).child("user-info")
                            .child("profile_picture").setValue(downloadUri.toString());
                }
            }
        });
    }
}
