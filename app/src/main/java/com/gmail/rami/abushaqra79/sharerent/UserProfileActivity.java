package com.gmail.rami.abushaqra79.sharerent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.ArrayList;
import java.util.UUID;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = UserProfileActivity.class.getSimpleName();
    public static final int GET_FROM_GALLERY = 200;
    private TextView emailAddress;
    private ArrayList<BabyGear> babyGears;
    private ListView listView;
    private CheckBox checkBox;
    private Spinner spinner;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String emailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        storageRef = storage.getReference();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        String userId = "";
        if (user != null) {
            userId = user.getUid();
        }

        emailAddress = findViewById(R.id.email_address);

        ReadAndWriteDatabase rwd = new ReadAndWriteDatabase();
        rwd.readDataForUser(userId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    emailText = snapshot.getValue().toString();
                    setTheTextFields(emailAddress, emailText);
                }
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
                {"Select an item..", "Stroller", "Bed", "Car Seat", "High Chair", "Bath Tubs", "Bouncer", "Sterilizer"};

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
                    addItemToList(type, typeDescription, typeRentPrice, imageUri);
                    wantToCloseDialog = true;
                }

                spinner.setSelection(0);

                if (wantToCloseDialog)
                    alertDialog.dismiss();
            }
        });
    }

    private void addItemToList(String type, String description, String price, Uri imageUri) {
        BabyGear babyGear = new BabyGear(type, description, price, imageUri);
        babyGears.add(babyGear);

        BabyGearAdapter babyGearAdapter = new BabyGearAdapter(UserProfileActivity.this,
                R.layout.baby_gear_details, babyGears);

        listView.setAdapter(babyGearAdapter);

        String path = "Rent_Items/" + UUID.randomUUID() + ".png";
        StorageReference itemsRef = storage.getReference(path);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("Type", type).build();

        UploadTask uploadTask = itemsRef.putFile(imageUri, metadata);

        // Code for showing progressDialog while uploading
        ProgressDialog progressDialog = new ProgressDialog(this);

        uploadTask.addOnSuccessListener(UserProfileActivity.this,
                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(UserProfileActivity.this,
                                "Item Added", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(UserProfileActivity.this,
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(UserProfileActivity.this,
                                        "Item not Added!", Toast.LENGTH_SHORT).show();
                            }
                        })
                .addOnProgressListener(UserProfileActivity.this,
                        new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                progressDialog.setTitle("Adding item...");
                                progressDialog.show();

                                double progress
                                        = (100.0
                                        * snapshot.getBytesTransferred()
                                        / snapshot.getTotalByteCount());
                                progressDialog.setMessage("Uploaded " + (int)progress + "%");
                            }
                        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                imageUri = selectedImage;
                checkBox.setChecked(true);
                checkBox.setError(null);
            }
        }
    }

    private void setTheTextFields(TextView tv, String text) {
        tv.setText(text);
    }
}