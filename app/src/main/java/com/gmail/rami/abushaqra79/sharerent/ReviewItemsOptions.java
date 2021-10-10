package com.gmail.rami.abushaqra79.sharerent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ReviewItemsOptions extends AppCompatActivity {

    // TODO check if you need the bundle variables for destination and dates

    private static final String TAG = ReviewItemsOptions.class.getSimpleName();
    private ArrayList<BabyGear> results;
    private ListView listView;

    /**
     * Obtain the previous instance of ChooseItemsActivity.java from the back stack
     * (WITHOUT RECREATING IT) when pressing the action bar back button.
     *
     * @return a new Intent targeting the defined parent activity
     */
    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        return super.getSupportParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_items_options);

        Bundle bundle = getIntent().getExtras();
        String destination = bundle.getString("Destination");
        String startDate = bundle.getString("Start Date");
        String endDate = bundle.getString("End Date");
        ArrayList<String> items = bundle.getStringArrayList("Selected Items");

        results = new ArrayList<>();
        listView = findViewById(R.id.review_items_list);

        for (int i = 0; i < items.size(); i++) {
            fetchResults(items.get(i));
        }
    }

    private void fetchResults(String type) {
        ReadAndWriteDatabase rwd = new ReadAndWriteDatabase(this);
        rwd.fetchData(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        if (child.hasChild("rent-items")) {
                            if (child.child("rent-items").hasChildren()) {
                                for (DataSnapshot subChild : child.child("rent-items").getChildren()) {
                                    String value = subChild.child("babyGearType").getValue().toString();
                                    if (value.equals(type)) {
                                        String description = subChild.child("babyGearDescription").getValue().toString();
                                        String price = subChild.child("rentPrice").getValue().toString();
                                        String imageUrl = subChild.child("imageUrl").getValue().toString();
                                        String storagePath = subChild.child("storagePath").getValue().toString();

                                        BabyGear gear = new BabyGear(value, description, price, imageUrl, storagePath);
                                        results.add(gear);
                                    }
                                }
                            }
                        }
                    }
                }

                BabyGearAdapter adapter = new BabyGearAdapter(ReviewItemsOptions.this,
                        R.layout.baby_gear_details, results);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error! Not able to get data");
            }
        });
    }
}