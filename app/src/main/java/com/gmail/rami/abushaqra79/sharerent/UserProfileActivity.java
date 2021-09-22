package com.gmail.rami.abushaqra79.sharerent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

public class UserProfileActivity extends AppCompatActivity {

    private ArrayList<BabyGear> babyGears;
    private Spinner spinner;
    private String title;
    private String titleDescription;
    private String titlePrice;
    private ListView listView;
    private BabyGearAdapter babyGearAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        babyGears = new ArrayList<>();

        listView = findViewById(R.id.list_of_offered_items);

        spinner = findViewById(R.id.baby_gear_list);
        String[] babyGearItems = new String[]
                {"", "Stroller", "Bed", "Car Seat", "High Chair", "Bath Tubs", "Bouncer", "Sterilizer"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, babyGearItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinner.getSelectedItem() != "") {
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

                title = spinner.getSelectedItem().toString();

                EditText descriptionText = dialogView.findViewById(R.id.title_description);
                titleDescription = descriptionText.getText().toString();

                if (TextUtils.isEmpty(titleDescription)) {
                    descriptionText.setError("Please add description");
                }

                EditText priceText = dialogView.findViewById(R.id.title_price);
                titlePrice = priceText.getText().toString();

                if (TextUtils.isEmpty(titlePrice)) {
                    priceText.setError("Please add rent price");
                }

                if (!TextUtils.isEmpty(titleDescription) && !TextUtils.isEmpty(titlePrice)) {
                    addItemToList(title, titleDescription, titlePrice);
                    wantToCloseDialog = true;
                }

                if(wantToCloseDialog)
                    alertDialog.dismiss();
            }
        });
    }

    private void addItemToList(String type, String description, String price) {
        BabyGear babyGear = new BabyGear(type, description, price);
        babyGears.add(babyGear);

        babyGearAdapter = new BabyGearAdapter(UserProfileActivity.this,
                R.layout.baby_gear_details, babyGears);

        listView.setAdapter(babyGearAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        babyGearAdapter.onActivityResult(requestCode, resultCode, data);
    }
}