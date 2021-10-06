package com.gmail.rami.abushaqra79.sharerent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReadAndWriteDatabase {

    private final String DATABASE_URL_LOCATION
            = "https://share-rent-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private final Activity activityContext;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    public ReadAndWriteDatabase(Activity activityContext) {
        this.activityContext = activityContext;
        database = FirebaseDatabase.getInstance(DATABASE_URL_LOCATION);
        databaseReference = database.getReference();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    /**
     * Creating new user node under 'Users'
     */
    public void createUser(String userId, String email) {
        databaseReference = database.getReference("Users");

        User user = new User(email);
        databaseReference.child(userId).setValue(user);

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("name", "");
        userInfo.put("phone_number", "");
        userInfo.put("location", "");
        userInfo.put("profile_picture", "");
        databaseReference.child(userId).child("user-info").setValue(userInfo);
    }

    public void readProfileInfoForUser(String userId, ValueEventListener listener) {
        if (!TextUtils.isEmpty(userId)) {
            databaseReference.child("Users").child(userId).child("user-info")
                    .addListenerForSingleValueEvent(listener);
        }
    }

    public void readRentItemsForUser(String userId, ChildEventListener listener) {
        if (!TextUtils.isEmpty(userId)) {
            databaseReference.child("Users").child(userId).child("rent-items")
                    .addChildEventListener(listener);
        }
    }

    public void saveUserInfo(String userId, String dbRef, String updatedInfo) {
        if (!TextUtils.isEmpty(userId)) {
            if (dbRef.equals("email")) {
                databaseReference.child("Users").child(userId).child(dbRef).setValue(updatedInfo);
            } else {
                databaseReference.child("Users").child(userId).child("user-info").child(dbRef)
                        .setValue(updatedInfo);
            }
        }
    }

    public void saveProfilePicture(String userId, Uri imageUri) {
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
                    Uri downloadUri = task.getResult();

                    databaseReference.child("Users").child(userId).child("user-info")
                            .child("profile_picture").setValue(downloadUri.toString());
                }
            }
        });
    }

    public void saveRentItems(String userId, String type, String description, String price, Uri imageUri) {
        String randomId = UUID.randomUUID() + ".png";
        String path = "Rent_Items/" + randomId;
        StorageReference ref = storageReference.child(path);

        UploadTask uploadTask = ref.putFile(imageUri);

        // Code for showing progressDialog while uploading
        ProgressDialog progressDialog = new ProgressDialog(activityContext);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(activityContext,"Item Added", Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(activityContext,"Item not Added!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        progressDialog.setTitle("Adding item...");
                        progressDialog.show();

                        double progress
                                = (100.0
                                * snapshot.getBytesTransferred()
                                / snapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploaded " + (int) progress + "%");
                    }
                });

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
                    String downloadUri = task.getResult().toString();

                    BabyGear babyGear = new BabyGear(type, description, price, downloadUri, randomId);

                    String itemKey = databaseReference.child("Users").child(userId)
                            .child("rent-items").push().getKey();

                    if (itemKey != null) {
                        databaseReference.child("Users").child(userId).child("rent-items")
                                .child(itemKey).setValue(babyGear);
                    }
                }
            }
        });
    }

    public void removeRentItems(String userId, String key, String path) {
        if (!TextUtils.isEmpty(userId)) {
            databaseReference.child("Users").child(userId).child("rent-items").child(key).removeValue();

            storageReference.child("Rent_Items").child(path).delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(activityContext, "File deleted successfully from Storage",
                                    Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(activityContext, "An error occurred!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}
