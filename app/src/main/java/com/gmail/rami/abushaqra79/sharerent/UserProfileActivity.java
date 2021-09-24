package com.gmail.rami.abushaqra79.sharerent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import java.util.ArrayList;

public class UserProfileActivity extends AppCompatActivity {

    public static final int GET_FROM_GALLERY = 200;
    private ArrayList<BabyGear> babyGears;
    private ListView listView;
    private CheckBox checkBox;
    private Spinner spinner;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

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
}