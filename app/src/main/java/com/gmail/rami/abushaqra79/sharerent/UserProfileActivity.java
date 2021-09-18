package com.gmail.rami.abushaqra79.sharerent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

public class UserProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);





        Spinner spinner = findViewById(R.id.baby_gear_list);
        String[] babyGearItems = new String[] {"Stroller", "Bed", "Car Seat", "High Chair",
                "Bath Tubs", "Bouncer", "Sterilizer"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, babyGearItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);







        ArrayList<BabyGear> babyGears = new ArrayList<>();

        BabyGearAdapter babyGearAdapter = new BabyGearAdapter(this,
                R.layout.baby_gear_details, babyGears);

        ListView listView = findViewById(R.id.list_of_offered_items);
        listView.setAdapter(babyGearAdapter);
    }
}