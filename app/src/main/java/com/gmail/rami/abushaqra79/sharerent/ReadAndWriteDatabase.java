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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
        databaseReference = database.getReference("Users");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    /**
     * Creating new user node under 'Users'
     */
    public void createUser(String userId, User user) {
        databaseReference.child(userId).child("user-info").setValue(user);
    }

    public void refreshToken(String userId) {
        if (!TextUtils.isEmpty(userId)) {
            String token = FirebaseMessaging.getInstance().getToken().getResult();
            databaseReference.child(userId).child("user-info").child("token").setValue(token);
        }
    }

    public void readProfileInfoForUser(String userId, ValueEventListener listener) {
        if (!TextUtils.isEmpty(userId)) {
            databaseReference.child(userId).child("user-info").addListenerForSingleValueEvent(listener);
        }
    }

    public void readRentItemsForUser(String userId, ChildEventListener listener) {
        if (!TextUtils.isEmpty(userId)) {
            databaseReference.child(userId).child("rent-items").addChildEventListener(listener);
        }
    }

    public void readSpecificItemInfo(String userId, String key, ValueEventListener listener) {
        if (!TextUtils.isEmpty(userId)) {
            databaseReference.child(userId).child("rent-items").child(key)
                    .addListenerForSingleValueEvent(listener);
        }
    }

    public void updateSpecificItemInfo(String userId, String key, String description, String price) {
        databaseReference.child(userId).child("rent-items").child(key).child("babyGearDescription")
                .setValue(description);

        databaseReference.child(userId).child("rent-items").child(key).child("rentPrice")
                .setValue(price);
    }

    public void updateItemPhoto(String userId, String key, Uri imageUri, FirebaseCallback callback) {
        String randomId = UUID.randomUUID() + ".png";
        String path = userId + "/Rent_Items/" + randomId;
        StorageReference ref = storageReference.child(path);

        UploadTask uploadTask = ref.putFile(imageUri);

        // Code for showing progressDialog while uploading
        ProgressDialog progressDialog = new ProgressDialog(activityContext);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(activityContext,"Item Updated", Toast.LENGTH_SHORT).show();

                callback.onResponse(true);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(activityContext,"Item not Updated!", Toast.LENGTH_SHORT).show();
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

                    databaseReference.child(userId).child("rent-items").child(key)
                            .child("imageUrl").setValue(downloadUri);

                    databaseReference.child(userId).child("rent-items").child(key)
                            .child("storagePath").setValue(randomId);
                }
            }
        });
    }

    public void fetchData(ValueEventListener listener) {
            databaseReference.addListenerForSingleValueEvent(listener);
    }

    public void saveUserInfo(String userId, String dbRef, String updatedInfo) {
        if (!TextUtils.isEmpty(userId)) {
            databaseReference.child(userId).child("user-info").child(dbRef).setValue(updatedInfo);
        }
    }

    public void saveProfilePicture(String userId, Uri imageUri) {
        String path = userId + "/Profile_Picture/user_profile_picture.png";
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

                    databaseReference.child(userId).child("user-info").child("profilePictureUrl")
                            .setValue(downloadUri.toString());
                }
            }
        });
    }

    public void saveRentItems(String userId, String type, String description, String price, Uri imageUri) {
        String randomId = UUID.randomUUID() + ".png";
        String path = userId + "/Rent_Items/" + randomId;
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

                    String itemKey = databaseReference.child(userId).child("rent-items")
                            .push().getKey();

                    if (itemKey != null) {
                        databaseReference.child(userId).child("rent-items").child(itemKey)
                                .setValue(babyGear);
                    }
                }
            }
        });
    }

    public void removeRentItems(String userId, String key, String path) {
        if (!TextUtils.isEmpty(userId)) {
            databaseReference.child(userId).child("rent-items").child(key).removeValue();

            storageReference.child(userId).child("Rent_Items").child(path).delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(activityContext, "Item deleted successfully",
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

    public void deleteUser(String userId, FirebaseCallback callback) {
        if (!TextUtils.isEmpty(userId)) {
            storageReference.child(userId).child("Rent_Items").listAll()
                    .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            for (StorageReference item : listResult.getItems()) {
                                item.delete();
                            }

                            callback.onResponse(true);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(activityContext, "An error occurred!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });

            storageReference.child(userId).child("Profile_Picture/user_profile_picture.png").delete();

            databaseReference.child(userId).removeValue();
        }
    }
}
