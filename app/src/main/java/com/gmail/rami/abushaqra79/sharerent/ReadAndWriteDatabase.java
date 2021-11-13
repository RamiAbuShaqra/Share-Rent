package com.gmail.rami.abushaqra79.sharerent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

/**
 * This class is dedicated for reading and writing to Firebase Database.
 */
public class ReadAndWriteDatabase {

    /**
     * Context requited to create progress dialog.
     */
    private final Activity activityContext;

    private final DatabaseReference databaseReference;
    private final StorageReference storageReference;

    public ReadAndWriteDatabase(Activity activityContext) {
        this.activityContext = activityContext;

        // The location of the database servers (Asia southeast-1)
        final String DATABASE_URL_LOCATION =
                "https://share-rent-default-rtdb.asia-southeast1.firebasedatabase.app/";

        // Get instance of the database
        databaseReference = FirebaseDatabase.getInstance(DATABASE_URL_LOCATION)
                .getReference("Users");

        // Get instance of Firebase storage
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    /**
     * Creates new user node under 'Users'.
     *
     * @param userId The auto generated ID for the user.
     * @param user   User object that contains user details.
     */
    public void createUser(String userId, User user) {
        databaseReference.child(userId).child("user-info").setValue(user);
    }

    /**
     * Gets the device registered token and refreshes it in the database.
     *
     * @param userId The auto generated ID for the user.
     */
    public void refreshToken(String userId) {
        if (!TextUtils.isEmpty(userId)) {
            String token = FirebaseMessaging.getInstance().getToken().getResult();
            databaseReference.child(userId).child("user-info").child("token").setValue(token);
        }
    }

    /**
     * Reads the user profile information from database.
     *
     * @param userId   The auto generated ID for the user.
     * @param listener A listener that detects any changes in the user profile information in
     *                 the database.
     */
    public void readProfileInfoForUser(String userId, ValueEventListener listener) {
        if (!TextUtils.isEmpty(userId)) {
            databaseReference.child(userId).child("user-info").addListenerForSingleValueEvent(listener);
        }
    }

    /**
     * Reads the rent items details from database.
     *
     * @param userId   The auto generated ID for the user.
     * @param listener A listener that detects any changes in the item details in the database.
     */
    public void readRentItemsForUser(String userId, ChildEventListener listener) {
        if (!TextUtils.isEmpty(userId)) {
            databaseReference.child(userId).child("rent-items").addChildEventListener(listener);
        }
    }

    /**
     * Reads information for a specific item from database.
     *
     * @param userId   The auto generated ID for the user.
     * @param key      The auto generated key for the item.
     * @param listener A listener that detects any changes in that specific item in
     *                 the database.
     */
    public void readSpecificItemInfo(String userId, String key, ValueEventListener listener) {
        if (!TextUtils.isEmpty(userId)) {
            databaseReference.child(userId).child("rent-items").child(key)
                    .addListenerForSingleValueEvent(listener);
        }
    }

    /**
     * Writes the updated item description and rent price to the database.
     *
     * @param userId      The auto generated ID for the user.
     * @param key         The auto generated key for the item.
     * @param description The updated item description.
     * @param price       The updated item rent price.
     */
    public void updateSpecificItemInfo(String userId, String key, String description, String price) {
        databaseReference.child(userId).child("rent-items").child(key).child("babyGearDescription")
                .setValue(description);

        databaseReference.child(userId).child("rent-items").child(key).child("rentPrice")
                .setValue(price);
    }

    /**
     * Uploads the item photo to Firebase storage and updates the database with the image Url.
     *
     * @param userId   The auto generated ID for the user.
     * @param key      The auto generated key for the item.
     * @param imageUri The selected image for the item.
     * @param callback The async callback that gets the response.
     */
    public void updateItemPhoto(String userId, String key, Uri imageUri, FirebaseCallback callback) {
        String randomId = UUID.randomUUID() + ".png";
        String path = userId + "/Rent_Items/" + randomId;
        StorageReference ref = storageReference.child(path);

        UploadTask uploadTask = ref.putFile(imageUri);

        // Code for showing progressDialog while uploading
        ProgressDialog progressDialog = new ProgressDialog(activityContext);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();
            Toast.makeText(activityContext, "Item Updated", Toast.LENGTH_SHORT).show();

            callback.onResponse(true);
        })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(activityContext, "Item not Updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(snapshot -> {
                    progressDialog.setTitle("Adding item...");
                    progressDialog.show();

                    double progress
                            = (100.0
                            * snapshot.getBytesTransferred()
                            / snapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                });

        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return ref.getDownloadUrl();
        });

        urlTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String downloadUri = task.getResult().toString();

                databaseReference.child(userId).child("rent-items").child(key)
                        .child("imageUrl").setValue(downloadUri);

                databaseReference.child(userId).child("rent-items").child(key)
                        .child("storagePath").setValue(randomId);
            }
        });
    }

    /**
     * Read the items and users information from the database.
     *
     * @param listener A listener that detects any changes in the data in the database.
     */
    public void fetchData(ValueEventListener listener) {
        databaseReference.addListenerForSingleValueEvent(listener);
    }

    /**
     * Writes the updated information of the user in the database.
     *
     * @param userId      The auto generated ID for the user.
     * @param dbRef       The reference key in which we need to update its value.
     * @param updatedInfo The updated value.
     */
    public void saveUserInfo(String userId, String dbRef, String updatedInfo) {
        if (!TextUtils.isEmpty(userId)) {
            databaseReference.child(userId).child("user-info").child(dbRef).setValue(updatedInfo);
        }
    }

    /**
     * Uploads the user profile picture to Firebase storage and updates the image Url
     * to the database.
     *
     * @param userId   The auto generated ID for the user.
     * @param imageUri The selected photo for the user.
     */
    public void saveProfilePicture(String userId, Uri imageUri) {
        String path = userId + "/Profile_Picture/user_profile_picture.png";
        StorageReference ref = storageReference.child(path);

        UploadTask uploadTask = ref.putFile(imageUri);

        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return ref.getDownloadUrl();
        });

        urlTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();

                if (downloadUri != null) {
                    databaseReference.child(userId).child("user-info").child("profilePictureUrl")
                            .setValue(downloadUri.toString());
                }
            }
        });
    }

    /**
     * Saves the baby gear object to the database.
     *
     * @param userId      The auto generated ID for the user.
     * @param type        The type of the item.
     * @param description The description of the item.
     * @param price       The rent price of the item.
     * @param imageUri    The selected photo for the item.
     */
    public void saveRentItems(String userId, String type, String description, String price, Uri imageUri) {
        String randomId = UUID.randomUUID() + ".png";
        String path = userId + "/Rent_Items/" + randomId;
        StorageReference ref = storageReference.child(path);

        UploadTask uploadTask = ref.putFile(imageUri);

        // Code for showing progressDialog while uploading
        ProgressDialog progressDialog = new ProgressDialog(activityContext);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();
            Toast.makeText(activityContext, "Item Added", Toast.LENGTH_SHORT).show();
        })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(activityContext, "Item not Added!", Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(snapshot -> {
                    progressDialog.setTitle("Adding item...");
                    progressDialog.show();

                    double progress
                            = (100.0
                            * snapshot.getBytesTransferred()
                            / snapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                });

        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            return ref.getDownloadUrl();
        });

        urlTask.addOnCompleteListener(task -> {
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
        });
    }

    /**
     * Deletes baby gear item from database and storage.
     *
     * @param userId The auto generated ID for the user.
     * @param key    The auto generated key for the item.
     * @param path   The path of image uri of the deleted item in Firebase storage.
     */
    public void removeRentItems(String userId, String key, String path) {
        if (!TextUtils.isEmpty(userId)) {
            databaseReference.child(userId).child("rent-items").child(key).removeValue();

            storageReference.child(userId).child("Rent_Items").child(path).delete()
                    .addOnSuccessListener(unused -> Toast.makeText(activityContext,
                            "Item deleted successfully", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e -> Toast.makeText(activityContext,
                            "An error occurred!", Toast.LENGTH_LONG).show());
        }
    }

    /**
     * Deletes the user and his information from database and Firebase storage.
     *
     * @param userId   The auto generated ID for the user.
     * @param callback The async callback that gets the response.
     */
    public void deleteUser(String userId, FirebaseCallback callback) {
        if (!TextUtils.isEmpty(userId)) {
            storageReference.child(userId).child("Rent_Items").listAll()
                    .addOnSuccessListener(listResult -> {
                        for (StorageReference item : listResult.getItems()) {
                            item.delete();
                        }

                        callback.onResponse(true);
                    })
                    .addOnFailureListener(e -> Toast.makeText(activityContext,
                            "An error occurred!", Toast.LENGTH_LONG).show());

            storageReference.child(userId).child("Profile_Picture/user_profile_picture.png").delete();
            databaseReference.child(userId).removeValue();
        }
    }
}
