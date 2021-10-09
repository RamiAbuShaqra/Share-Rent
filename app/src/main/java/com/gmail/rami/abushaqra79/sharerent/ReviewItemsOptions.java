package com.gmail.rami.abushaqra79.sharerent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ReviewItemsOptions extends AppCompatActivity {

    // TODO add code to return back to previous version of ChooseItemsActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_items_options);

        Bundle bundle = getIntent().getExtras();
        String destination = bundle.getString("Destination");
        String startDate = bundle.getString("Start Date");
        String endDate = bundle.getString("End Date");
        ArrayList<String> items = bundle.getStringArrayList("Selected Items");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.baby_gear_details,
                R.id.item_description, items);

        ListView listView = findViewById(R.id.review_items_list);
        listView.setAdapter(adapter);
    }
}