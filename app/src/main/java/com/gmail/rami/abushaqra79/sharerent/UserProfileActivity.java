package com.gmail.rami.abushaqra79.sharerent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
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

    // TODO handle delete or update the items properly
    // TODO fix the listview inside scrollview

    private static final String TAG = UserProfileActivity.class.getSimpleName();
    public static final int GET_FROM_GALLERY = 200;
    private TextView userName;
    private TextView phoneNumber;
    private TextView userLocation;
    private ImageView editName;
    private ImageView editPhone;
    private ImageView editLocation;
    private TextView changeEmail;
    private TextView changePassword;
    private ProgressBar loadingSpinner;
    private ArrayList<BabyGear> babyGears;
    private ArrayList<String> keysList;
    private ListView listView;
    private BabyGearAdapter babyGearAdapter;
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
                auth.signOut();
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

        loadingSpinner = findViewById(R.id.loading_spinner);
        loadingSpinner.setVisibility(View.VISIBLE);

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
        phoneNumber = findViewById(R.id.phone_number);
        userLocation = findViewById(R.id.user_location);

        editName = findViewById(R.id.edit_name);
        editPhone = findViewById(R.id.edit_phone);
        editLocation = findViewById(R.id.edit_location);

        editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfoDialog(R.layout.update_name_dialog, userName, "name");
            }
        });

        editPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfoDialog(R.layout.update_phone_dialog, phoneNumber, "phoneNumber");
            }
        });

        editLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfoDialog(R.layout.update_location_dialog, userLocation, "location");
            }
        });

        changeEmail = findViewById(R.id.change_email);
        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.change_email_dialog, null);
                builder.setView(dialogView)
                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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

                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean wantToCloseDialog = false;
                        TextInputLayout inputLayout = dialogView.findViewById(R.id.new_email_input_layout);
                        EditText newEmail = dialogView.findViewById(R.id.new_email);
                        String updatedEmail = newEmail.getText().toString().trim();

                        if (user != null && !updatedEmail.equals("") &&
                                android.util.Patterns.EMAIL_ADDRESS.matcher(updatedEmail).matches()) {
                            user.updateEmail(updatedEmail)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(UserProfileActivity.this,
                                                        "Email address is updated." +
                                                                "\nPlease sign in with new email.",
                                                        Toast.LENGTH_LONG).show();

                                                rwd.saveUserInfo(userId, "email", updatedEmail);

                                                auth.signOut();
                                                Intent intent = new Intent(
                                                        UserProfileActivity.this,
                                                        LoginActivity.class);
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(UserProfileActivity.this,
                                                        "Failed to update email!",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                            wantToCloseDialog = true;

                        } else if (updatedEmail.equals("") ||
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(updatedEmail).matches()) {
                            inputLayout.setError("Enter a valid e-mail address!");
                        }

                        newEmail.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                inputLayout.setError(null);
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                            }
                        });

                        if (wantToCloseDialog) {
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        changePassword = findViewById(R.id.change_password);
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.change_password_dialog, null);
                builder.setView(dialogView)
                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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

                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean wantToCloseDialog = false;
                        TextInputLayout currentLayout = dialogView.findViewById(R.id.current_password_layout);
                        TextInputLayout newLayout = dialogView.findViewById(R.id.new_password_layout);
                        TextInputLayout confirmLayout = dialogView.findViewById(R.id.confirm_password_layout);
                        EditText currentPW = dialogView.findViewById(R.id.current_password);
                        EditText newPW = dialogView.findViewById(R.id.new_password);
                        EditText confirmPW = dialogView.findViewById(R.id.confirm_new_password);

                        String currentPassword = currentPW.getText().toString().trim();
                        String newPassword = newPW.getText().toString().trim();
                        String confirmPassword = confirmPW.getText().toString().trim();

                        if (user != null && !currentPassword.equals("") && !newPassword.equals("")
                                && !confirmPassword.equals("")) {

                            if (newPassword.length() < 8) {
                                newLayout.setError("Password should be minimum 8 characters.");
                                return;
                            }
                            if (newPassword.equals(currentPassword)) {
                                newLayout.setError("New password is the same of current password!");
                                return;
                            }
                            if (!confirmPassword.equals(newPassword)) {
                                confirmLayout.setError("Confirm password and new password aren't match.");
                                return;
                            }

                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(UserProfileActivity.this,
                                                        "Password is changed.", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(UserProfileActivity.this,
                                                        "Failed to change password!", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                            wantToCloseDialog = true;

                        } else {
                            if (currentPassword.equals("")) {
                                currentLayout.setError("Enter your current password.");
                            }
                            if (newPassword.equals("")) {
                                newLayout.setError("Enter a new password.");
                            }
                            if (confirmPassword.equals("")) {
                                confirmLayout.setError("Enter the new password again.");
                            }
                        }

                        currentPW.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                currentLayout.setError(null);
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                            }
                        });

                        newPW.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                newLayout.setError(null);
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                            }
                        });

                        confirmPW.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                confirmLayout.setError(null);
                            }

                            @Override
                            public void afterTextChanged(Editable s) {
                            }
                        });

                        if (wantToCloseDialog) {
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        rwd = new ReadAndWriteDatabase(this);
        rwd.readProfileInfoForUser(userId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nameText = snapshot.child("name").getValue().toString();
                    setTheTextFields(userName, nameText);

                    String phoneText = snapshot.child("phoneNumber").getValue().toString();
                    setTheTextFields(phoneNumber, phoneText);

                    String locationText = snapshot.child("location").getValue().toString();
                    setTheTextFields(userLocation, locationText);

                    String profilePictureUrl = snapshot.child("profilePictureUrl").getValue().toString();

                    if (!TextUtils.isEmpty(profilePictureUrl)) {
                        StorageReference reference = storageRef.child("pictures/").child(userId + "/")
                                .child("user_profile_picture.png");

                        Glide.with(UserProfileActivity.this)
                                .load(reference)
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                                Target<Drawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model,
                                                                   Target<Drawable> target, DataSource dataSource,
                                                                   boolean isFirstResource) {
                                        loadingSpinner.setVisibility(View.GONE);
                                        return false;
                                    }
                                })
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(profilePicture);
                    } else {
                        loadingSpinner.setVisibility(View.GONE);
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
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    String itemType = snapshot.child("babyGearType").getValue().toString();
                    String itemDescription = snapshot.child("babyGearDescription").getValue().toString();
                    String itemRentPrice = snapshot.child("rentPrice").getValue().toString();
                    String itemPhotoUrl = snapshot.child("imageUrl").getValue().toString();
                    String storagePath = snapshot.child("storagePath").getValue().toString();

                    BabyGear babyGear = new BabyGear(itemType, itemDescription, itemRentPrice,
                            itemPhotoUrl, storagePath);

                    babyGears.add(babyGear);
                    keysList.add(snapshot.getKey());

                    babyGearAdapter = new BabyGearAdapter(
                            UserProfileActivity.this, R.layout.baby_gear_details, babyGears);

                    listView.setAdapter(babyGearAdapter);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                BabyGear babyGear = snapshot.getValue(BabyGear.class);

                babyGears.remove(babyGear);
                keysList.remove(snapshot.getKey());

                babyGearAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read data", error.toException());
            }
        });

        babyGears = new ArrayList<>();
        keysList = new ArrayList<>();

        listView = findViewById(R.id.list_of_offered_items);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
                builder.setMessage("Delete item?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BabyGear currentBabyGear = babyGearAdapter.getItem(position);

                                String key = keysList.get(position);
                                String path = currentBabyGear.getStoragePath();

                                rwd.removeRentItems(userId, key, path);

                                finish();
                                startActivity(getIntent());
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
        });

        spinner = findViewById(R.id.baby_gear_list);
        String[] babyGearItems = new String[]
                {"Select an item..", "Stroller", "Bed", "Car Seat", "High Chair",
                        "Bath Tub", "Bouncer", "Sterilizer"};

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
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
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

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
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

                if (wantToCloseDialog) {
                    alertDialog.dismiss();
                }
            }
        });
    }

    private void updateUserInfoDialog(int layoutId, TextView textField, String dbRef) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        View dialogView = inflater.inflate(layoutId, null);

        TextInputLayout inputLayout = dialogView.findViewById(R.id.info_input_layout);
        EditText editText = dialogView.findViewById(R.id.update_info);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                inputLayout.setError(null);
            }
        });

        builder.setView(dialogView)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean wantToCloseDialog = false;

                String updatedInfo = editText.getText().toString();

                if (TextUtils.isEmpty(updatedInfo)) {
                    inputLayout.setError("Please fill this field");
                }

                if (!TextUtils.isEmpty(updatedInfo)) {
                    setTheTextFields(textField, updatedInfo);
                    rwd.saveUserInfo(userId, dbRef, updatedInfo);
                    wantToCloseDialog = true;
                }

                if (wantToCloseDialog) {
                    alertDialog.dismiss();
                }
            }
        });
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