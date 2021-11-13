package com.gmail.rami.abushaqra79.sharerent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

/**
 * Activity for the user, in which personal information and offered items can be displayed,
 * modified, or deleted.
 */
public class UserProfileActivity extends MainActivity {

    /**
     * Tag for the log messages
     */
    private static final String TAG = UserProfileActivity.class.getSimpleName();

    /**
     * Request code for getting activity results
     */
    public static final int GET_FROM_GALLERY = 200;

    private StorageReference storage;
    private FirebaseAuth auth;
    private FirebaseUser user;

    private ReadAndWriteDatabase rwd;
    private BabyGearAdapter babyGearAdapter;
    private ArrayList<BabyGear> babyGears;
    private ArrayList<String> keysList;
    private TextView userName;
    private TextView phoneNumber;
    private TextView userLocation;
    private TextView listHeader;
    private ListView listView;
    private ProgressBar progressBar;
    private CheckBox checkBox;
    private Spinner spinner;
    private Uri imageUri;
    private String userId;
    private ImageView profilePicture;
    private boolean isProfilePicture = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        // Hide the shopping cart menu item
        menu.findItem(R.id.shopping_cart).setVisible(false);
        // Hide the notification menu item
        menu.findItem(R.id.notification).setVisible(false);
        menu.findItem(R.id.login_menu_item).setVisible(false);
        menu.findItem(R.id.logout_menu_item).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout_menu_item) {
            auth.signOut();
            Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Create a storage reference for our app
        storage = FirebaseStorage.getInstance().getReference();

        // Get auth instance
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        userId = "";
        if (user != null) {
            userId = user.getUid();
        }

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        rwd = new ReadAndWriteDatabase(this);
        rwd.refreshToken(userId);

        profilePicture = findViewById(R.id.profile_picture);
        profilePicture.setOnClickListener(v -> {
            isProfilePicture = true;
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, GET_FROM_GALLERY);
        });

        userName = findViewById(R.id.user_name);
        phoneNumber = findViewById(R.id.phone_number);
        userLocation = findViewById(R.id.user_location);

        ImageView editName = findViewById(R.id.edit_name);
        ImageView editPhone = findViewById(R.id.edit_phone);
        ImageView editLocation = findViewById(R.id.edit_location);

        editName.setOnClickListener(v -> updateUserInfoDialog(R.layout.update_name_dialog,
                userName, "name"));

        editPhone.setOnClickListener(v -> updateUserInfoDialog(R.layout.update_phone_dialog,
                phoneNumber, "phoneNumber"));

        editLocation.setOnClickListener(v -> updateUserLocationDialog());

        TextView changeEmail = findViewById(R.id.change_email);
        changeEmail.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.change_email_dialog, null);
            builder.setView(dialogView)
                    .setPositiveButton("Update", (dialog, which) -> {
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                boolean wantToCloseDialog = false;
                TextInputLayout inputLayout = dialogView.findViewById(R.id.new_email_input_layout);
                EditText newEmail = dialogView.findViewById(R.id.new_email);
                String updatedEmail = newEmail.getText().toString().trim();

                if (user != null && !updatedEmail.equals("") &&
                        android.util.Patterns.EMAIL_ADDRESS.matcher(updatedEmail).matches()) {
                    user.updateEmail(updatedEmail)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(UserProfileActivity.this,
                                            "Email address is updated." +
                                                    "\nPlease sign in with new email.",
                                            Toast.LENGTH_LONG).show();

                                    rwd.saveUserInfo(userId, "email", updatedEmail);

                                    auth.signOut();
                                    Intent intent = new Intent(UserProfileActivity.this,
                                            LoginActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(UserProfileActivity.this,
                                            "Failed to update email!", Toast.LENGTH_LONG).show();
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
            });
        });

        TextView changePassword = findViewById(R.id.change_password);
        changePassword.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.change_password_dialog, null);
            builder.setView(dialogView)
                    .setPositiveButton("Update", (dialog, which) -> {
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v12 -> {
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
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(UserProfileActivity.this,
                                            "Password is changed.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(UserProfileActivity.this,
                                            "Failed to change password!", Toast.LENGTH_LONG).show();
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
            });
        });

        TextView deleteAccount = findViewById(R.id.delete_account);
        deleteAccount.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
            builder.setMessage("Are you sure you want to delete your account?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        progressBar.setVisibility(View.VISIBLE);
                        if (user != null) {
                            rwd.deleteUser(userId, value -> {
                                if (value) {
                                    user.delete()
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(
                                                            UserProfileActivity.this,
                                                            "Your account is deleted.",
                                                            Toast.LENGTH_LONG).show();

                                                    Intent intent = new Intent(
                                                            UserProfileActivity.this,
                                                            SignupActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(
                                                            UserProfileActivity.this,
                                                            "Failed to delete your account!",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                                progressBar.setVisibility(View.GONE);
                                            });
                                }
                            });

                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        // Read user profile information from database and update the UI
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
                        StorageReference reference = storage.child(userId)
                                .child("/Profile_Picture/user_profile_picture.png");

                        Glide.with(UserProfileActivity.this)
                                .load(reference)
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e,
                                                                Object model, Target<Drawable> target,
                                                                boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model,
                                                                   Target<Drawable> target,
                                                                   DataSource dataSource,
                                                                   boolean isFirstResource) {
                                        progressBar.setVisibility(View.GONE);
                                        return false;
                                    }
                                })
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(profilePicture);
                    } else {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read data", error.toException());
            }
        });

        // Read the items details from database and update the UI
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

                    if (babyGears.size() == 0) {
                        listHeader.setText(R.string.empty_list);
                    } else {
                        listHeader.setText(R.string.edit_or_delete);
                    }

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

        // List for offered items
        babyGears = new ArrayList<>();
        // List for auto generated keys for the added items
        keysList = new ArrayList<>();

        listHeader = findViewById(R.id.list_header);

        listView = findViewById(R.id.list_of_offered_items);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
            builder.setMessage("If you want to edit the item information, please select 'EDIT'\n" +
                    "\nIf you want to delete the item, please select 'DELETE'")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        BabyGear currentBabyGear = babyGearAdapter.getItem(position);

                        String key = keysList.get(position);
                        String path = currentBabyGear.getStoragePath();

                        rwd.removeRentItems(userId, key, path);

                        finish();
                        startActivity(getIntent());
                    })
                    .setNegativeButton("Edit", (dialog, which) -> editItemInformation(position))
                    .setNeutralButton("Cancel", (dialog, which) -> {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        spinner = findViewById(R.id.baby_gear_list);
        String[] babyGearItems = new String[]
                {"Select an item..", "Stroller", "Bed", "Car Seat", "High Chair",
                        "Bath Tub", "Bouncer", "Sterilizer"};

        // Adapter used to display the items in spinner
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

    /**
     * Handles saving items details in the database.
     */
    private void showCompleteInformationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate and set the layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.add_information_dialog, null);

        checkBox = dialogView.findViewById(R.id.upload_check);

        TextView uploadBtn = dialogView.findViewById(R.id.upload_btn);
        uploadBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, GET_FROM_GALLERY);
        });

        builder.setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (dialog != null) {
                        spinner.setSelection(0);
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            boolean wantToCloseDialog = false;

            TextInputLayout descriptionLayout = dialogView.findViewById(R.id.description_layout);
            TextInputLayout priceLayout = dialogView.findViewById(R.id.price_layout);
            EditText descriptionText = dialogView.findViewById(R.id.title_description);
            EditText priceText = dialogView.findViewById(R.id.title_price);

            descriptionText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    descriptionLayout.setError(null);
                }
            });

            priceText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    priceLayout.setError(null);
                }
            });

            String type = spinner.getSelectedItem().toString();
            String typeDescription = descriptionText.getText().toString();
            String typeRentPrice = priceText.getText().toString().trim();

            if (TextUtils.isEmpty(typeDescription)) {
                descriptionLayout.setError("Please add description");
            }

            if (TextUtils.isEmpty(typeRentPrice)) {
                priceLayout.setError("Please add rent price");
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
        });
    }

    /**
     * Creates a dialog so the user can enter and update his personal information, and then
     * save it to the database.
     *
     * @param layoutId  The layout to be inflated as dialog box.
     * @param textField The TextView to be updated with the new information.
     * @param dbRef     The database reference to be updated.
     */
    private void updateUserInfoDialog(int layoutId, TextView textField, String dbRef) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(layoutId, null);

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
                .setPositiveButton("Update", (dialog, which) -> {
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
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
        });
    }

    /**
     * Creates a dialog so the user can update his location, and then save it to the database.
     */
    private void updateUserLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.update_location_dialog, null);

        Spinner cities = dialogView.findViewById(R.id.cities_list);

        // Adapter used to display cities names from the spinner
        ArrayAdapter<CharSequence> citiesAdapter = ArrayAdapter.createFromResource(
                UserProfileActivity.this, R.array.cities_list,
                android.R.layout.simple_spinner_item);
        citiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cities.setAdapter(citiesAdapter);

        builder.setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            boolean wantToCloseDialog = false;

            String city = cities.getSelectedItem().toString();

            if (city.equals("Select City..")) {
                ((TextView) cities.getSelectedView()).setError("Please select a city");
            } else {
                setTheTextFields(userLocation, city);
                rwd.saveUserInfo(userId, "location", city);
                wantToCloseDialog = true;
            }

            if (wantToCloseDialog) {
                alertDialog.dismiss();
            }
        });
    }

    /**
     * Edits one or more information for a specific baby gear item from the offered list.
     *
     * @param index The position of the item to be edited from the list.
     */
    private void editItemInformation(int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.add_information_dialog, null);

        EditText description = dialogView.findViewById(R.id.title_description);
        EditText price = dialogView.findViewById(R.id.title_price);
        checkBox = dialogView.findViewById(R.id.upload_check);

        TextView uploadBtn = dialogView.findViewById(R.id.upload_btn);
        uploadBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, GET_FROM_GALLERY);
        });

        rwd.readSpecificItemInfo(userId, keysList.get(index), new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String savedDescription = snapshot.child("babyGearDescription").getValue().toString();
                    String savedPrice = snapshot.child("rentPrice").getValue().toString();

                    description.setText(savedDescription);
                    price.setText(savedPrice);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read data", error.toException());
            }
        });

        builder.setView(dialogView)
                .setPositiveButton("Edit", (dialog, which) -> {
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newDescription = description.getText().toString();
            String newPrice = price.getText().toString();

            rwd.updateSpecificItemInfo(userId, keysList.get(index), newDescription, newPrice);

            if (checkBox.isChecked()) {
                alertDialog.dismiss();
                rwd.updateItemPhoto(userId, keysList.get(index), imageUri, value -> {
                    if (value) {
                        finish();
                        startActivity(getIntent());
                    }
                });
            } else {
                alertDialog.dismiss();

                finish();
                startActivity(getIntent());
            }
        });
    }

    /**
     * Called when the activity is launched with the request code and gets the results.
     *
     * @param requestCode The originally supplied request code to identify who
     *                    this result came from.
     * @param resultCode  The result code returned by the child activity.
     * @param data        An Intent, which can return result data to the caller.
     */
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

    /**
     * This helper method sets a specific text to a specific TextView.
     *
     * @param tv   A TextView to set the text for.
     * @param text The string text to be set on the TextView.
     */
    private void setTheTextFields(TextView tv, String text) {
        tv.setText(text);
    }
}