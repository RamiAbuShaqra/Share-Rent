package com.gmail.rami.abushaqra79.sharerent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ReviewItemsOptions extends MainActivity {

    private static final String TAG = ReviewItemsOptions.class.getSimpleName();
    private ArrayList<BabyGear> results;
    private ArrayList<User> users;
    private ListView listView;

    /** TextView that is displayed when the list is empty */
    private TextView emptyStateTextView;

    /** Spinner that is displayed until the app fetches the data */
    private ProgressBar progressBar;

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
        ArrayList<String> items = bundle.getStringArrayList("Selected Items");

        results = new ArrayList<>();
        users = new ArrayList<>();

        progressBar = findViewById(R.id.loading_spinner);
        emptyStateTextView = findViewById(R.id.empty_view);
        listView = findViewById(R.id.review_items_list);
        listView.setEmptyView(emptyStateTextView);

        for (int i = 0; i < items.size(); i++) {
            fetchResults(items.get(i), destination);
        }
    }

    private void fetchResults(String type, String location) {
        ReadAndWriteDatabase rwd = new ReadAndWriteDatabase(this);
        rwd.fetchData(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        if (child.hasChild("rent-items")) {
                            if (child.child("rent-items").hasChildren()) {
                                for (DataSnapshot subChild : child.child("rent-items").getChildren()) {
                                    String typeValue = subChild.child("babyGearType")
                                            .getValue().toString();

                                    if (typeValue.equals(type)) {
                                        String locationValue = child.child("user-info")
                                                .child("location").getValue().toString();
                                        String[] city = location.split(",");

                                        if (locationValue.equals(city[0])) {
                                            String description = subChild.child("babyGearDescription")
                                                    .getValue().toString();

                                            String price = subChild.child("rentPrice")
                                                    .getValue().toString();

                                            String imageUrl = subChild.child("imageUrl")
                                                    .getValue().toString();

                                            String storagePath = subChild.child("storagePath")
                                                    .getValue().toString();

                                            BabyGear gear = new BabyGear(typeValue, description,
                                                    price, imageUrl, storagePath);
                                            results.add(gear);

                                            String email = child.child("user-info")
                                                    .child("email").getValue().toString();

                                            String name = child.child("user-info")
                                                    .child("name").getValue().toString();

                                            String phone = child.child("user-info")
                                                    .child("phoneNumber").getValue().toString();

                                            String location = child.child("user-info")
                                                    .child("location").getValue().toString();

                                            String picture = child.child("user-info")
                                                    .child("profilePictureUrl").getValue().toString();

                                            User user = new User(email, name, phone, location, picture);
                                            users.add(user);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                progressBar.setVisibility(View.GONE);
                emptyStateTextView.setText("No results found.");

                BabyGearAdapter adapter = new BabyGearAdapter(ReviewItemsOptions.this,
                        R.layout.baby_gear_details, results, false);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        BabyGear currentGear = adapter.getItem(position);
                        User currentUser = users.get(position);

                        Intent intent = new Intent(ReviewItemsOptions.this,
                                SelectedItemActivity.class);
                        intent.putExtra("Type", currentGear.getBabyGearType());
                        intent.putExtra("Description", currentGear.getBabyGearDescription());
                        intent.putExtra("Rent Price", currentGear.getRentPrice());
                        intent.putExtra("Image URL", currentGear.getImageUrl());
                        intent.putExtra("Storage Path", currentGear.getStoragePath());
                        intent.putExtra("Item Provider Picture", currentUser.getProfilePictureUrl());
                        intent.putExtra("Item Provider Email", currentUser.getEmail());
                        intent.putExtra("Item Provider Phone Number", currentUser.getPhoneNumber());
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error! Not able to get data");
            }
        });
    }
}