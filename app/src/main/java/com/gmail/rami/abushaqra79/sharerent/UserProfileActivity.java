package com.gmail.rami.abushaqra79.sharerent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class UserProfileActivity extends MainActivity {

    private static final String TAG = UserProfileActivity.class.getSimpleName();
    public static final int GET_FROM_GALLERY = 200;
    private TextView userName;
    private TextView emailAddress;
    private TextView phoneNumber;
    private TextView userLocation;
    private ImageView editName;
    private ImageView editEmail;
    private ImageView editPhone;
    private ImageView editLocation;
    private ArrayList<BabyGear> babyGears;
    private ListView listView;
    private CheckBox checkBox;
    private Spinner spinner;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String userId;
    private ReadAndWriteDatabase rwd;
    private ImageView profilePicture;
    private boolean isProfilePicture = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        menu.findItem(R.id.shopping_cart).setVisible(false);
        menu.findItem(R.id.login_menu_item).setVisible(false);
        menu.findItem(R.id.logout_menu_item).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_menu_item:
                Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        storage = FirebaseStorage.getInstance();

        // Create a storage reference for our app
        storageRef = storage.getReference();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        userId = "";
        if (user != null) {
            userId = user.getUid();
        }

        profilePicture = findViewById(R.id.profile_picture);
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isProfilePicture = true;
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, GET_FROM_GALLERY);
            }
        });

        userName = findViewById(R.id.user_name);
        emailAddress = findViewById(R.id.email_address);
        phoneNumber = findViewById(R.id.phone_number);
        userLocation = findViewById(R.id.user_location);

        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        editLocation = findViewById(R.id.edit_location);

        editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfoDialog(R.layout.update_name_dialog,
                        userName, "name");
            }
        });

        editEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfoDialog(R.layout.update_email_dialog,
                        emailAddress, "email");
            }
        });

        editPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfoDialog(R.layout.update_phone_dialog,
                        phoneNumber, "phone_number");
            }
        });

        editLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfoDialog(R.layout.update_location_dialog,
                        userLocation, "location");
            }
        });

        rwd = new ReadAndWriteDatabase(this);
        rwd.readProfileInfoForUser(userId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String emailText = snapshot.child("email").getValue().toString();
                    setTheTextFields(emailAddress, emailText);

                    String nameText = snapshot.child("user-info").child("name")
                            .getValue().toString();
                    setTheTextFields(userName, nameText);

                    String phoneText = snapshot.child("user-info").child("phone_number")
                            .getValue().toString();
                    setTheTextFields(phoneNumber, phoneText);

                    String locationText = snapshot.child("user-info").child("location")
                            .getValue().toString();
                    setTheTextFields(userLocation, locationText);

                    String profilePictureUrl = snapshot.child("user-info").child("profile_picture")
                            .getValue().toString();

                    if (!TextUtils.isEmpty(profilePictureUrl)) {
                        StorageReference reference = storageRef
                                .child("pictures/user_profile_picture.png");

                        Glide.with(UserProfileActivity.this)
                                .load(reference)
                                // TODO think about setting a signature instead of disabling the cache
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(profilePicture);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read data", error.toException());
            }
        });

        rwd.readRentItemsForUser(userId, new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot,
                                     @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    String itemType = snapshot.child("babyGearType").getValue().toString();
                    String itemDescription = snapshot.child("babyGearDescription")
                            .getValue().toString();
                    String itemRentPrice = snapshot.child("rentPrice").getValue().toString();
                    String itemPhotoUrl = snapshot.child("imageUrl").getValue().toString();

                    BabyGear babyGear = new BabyGear(itemType, itemDescription,
                            itemRentPrice, itemPhotoUrl);
                    babyGears.add(babyGear);

                    BabyGearAdapter babyGearAdapter = new BabyGearAdapter(
                            UserProfileActivity.this, R.layout.baby_gear_details, babyGears);

                    listView.setAdapter(babyGearAdapter);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot,
                                       @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot,
                                     @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read data", error.toException());
            }
        });

        babyGears = new ArrayList<>();

        listView = findViewById(R.id.list_of_offered_items);

        spinner = findViewById(R.id.baby_gear_list);
        String[] babyGearItems = new String[]
                {"Select an item..", "Stroller", "Bed", "Car Seat", "High Chair",
                        "Bath Tubs", "Bouncer", "Sterilizer"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, babyGearItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinner.getSelectedItem() != "Select an item..") {
                    showCompleteInformationDialog();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void showCompleteInformationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View dialogView = inflater.inflate(R.layout.add_information_dialog, null);

        checkBox = dialogView.findViewById(R.id.upload_check);

        Button btn = dialogView.findViewById(R.id.upload_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, GET_FROM_GALLERY);
            }
        });

        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            spinner.setSelection(0);
                            dialog.dismiss();
                        }
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean wantToCloseDialog = false;

                String type = spinner.getSelectedItem().toString();

                EditText descriptionText = dialogView.findViewById(R.id.title_description);
                String typeDescription = descriptionText.getText().toString();

                if (TextUtils.isEmpty(typeDescription)) {
                    descriptionText.setError("Please add description");
                }

                EditText priceText = dialogView.findViewById(R.id.title_price);
                String typeRentPrice = priceText.getText().toString();

                if (TextUtils.isEmpty(typeRentPrice)) {
                    priceText.setError("Please add rent price");
                }

                if (!checkBox.isChecked()) {
                    checkBox.setError("Please upload a photo");
                }

                if (!TextUtils.isEmpty(typeDescription) && !TextUtils.isEmpty(typeRentPrice) &&
                    checkBox.isChecked()) {
                    rwd.saveRentItems(userId, type, typeDescription, typeRentPrice, imageUri);
                    wantToCloseDialog = true;
                }

                spinner.setSelection(0);

                if (wantToCloseDialog)
                    alertDialog.dismiss();
            }
        });
    }

    private void updateUserInfoDialog(int layoutId, TextView textField, String dbRef) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(layoutId, null);

        EditText editText = dialogView.findViewById(R.id.update_info);

        builder.setView(dialogView)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String updatedInfo = editText.getText().toString();
                        setTheTextFields(textField, updatedInfo);
                        rwd.saveUserInfo(userId, dbRef, updatedInfo);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                if (isProfilePicture) {
                    profilePicture.setImageURI(selectedImage);
                    rwd.saveProfilePicture(userId, selectedImage);
                    isProfilePicture = false;
                } else {
                    imageUri = selectedImage;
                    checkBox.setChecked(true);
                    checkBox.setError(null);
                }
            }
        }
        isProfilePicture = false;
    }

    private void setTheTextFields(TextView tv, String text) {
        tv.setText(text);
    }
}