package com.gmail.rami.abushaqra79.sharerent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Activity for reviewing the results of baby gear items based on the search criteria.
 */
public class ReviewItemsOptions extends MainActivity {

    /**
     * Tag for the log messages.
     */
    private static final String TAG = ReviewItemsOptions.class.getSimpleName();

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView emptyStateTextView;

    /**
     * Spinner that is displayed until the app fetches the data
     */
    private ProgressBar progressBar;

    private ArrayList<BabyGear> results;
    private ArrayList<User> users;
    private ListView listView;
    private String startDate;
    private String endDate;
    private int totalDays;

    /**
     * Obtain the previous instance of ChooseItemsActivity.java from the back stack
     * (WITHOUT RECREATING IT) when pressing the action bar back button.
     *
     * @return a new Intent targeting the defined parent activity.
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

        // Get the intent with its extras
        Bundle bundle = getIntent().getExtras();
        String destination = bundle.getString("Destination");
        ArrayList<String> items = bundle.getStringArrayList("Selected Items");
        startDate = bundle.getString("Start Date");
        endDate = bundle.getString("End Date");
        totalDays = bundle.getInt("Total Days");

        results = new ArrayList<>();
        users = new ArrayList<>();

        progressBar = findViewById(R.id.progress_bar);
        emptyStateTextView = findViewById(R.id.empty_view);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        // If there is no network connection, notify the user
        if (activeNetwork == null || !activeNetwork.isConnected()) {
            progressBar.setVisibility(View.GONE);
            emptyStateTextView.setText(R.string.no_internet_connection);
        }

        listView = findViewById(R.id.review_items_list);
        listView.setEmptyView(emptyStateTextView);

        for (int i = 0; i < items.size(); i++) {
            fetchResults(items.get(i), destination);
        }
    }

    /**
     * This helper method searches in the database for items that matches the search criteria
     * entered by the customer.
     *
     * @param type     Baby gear type.
     * @param location Travel destination.
     */
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

                                            String token = child.child("user-info")
                                                    .child("token").getValue().toString();

                                            User user = new User(email, name, phone, location, picture, token);
                                            users.add(user);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                progressBar.setVisibility(View.GONE);
                // Set empty state results
                emptyStateTextView.setText(R.string.no_results_found);

                // Adapter used to display the results of items
                BabyGearAdapter adapter = new BabyGearAdapter(ReviewItemsOptions.this,
                        R.layout.baby_gear_details, results);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener((parent, view, position, id) -> {
                    BabyGear currentGear = adapter.getItem(position);
                    User currentUser = users.get(position);

                    Intent intent = new Intent(ReviewItemsOptions.this,
                            SelectedItemActivity.class);
                    intent.putExtra("Type", currentGear.getBabyGearType());
                    intent.putExtra("Description", currentGear.getBabyGearDescription());
                    intent.putExtra("Rent Price", currentGear.getRentPrice());
                    intent.putExtra("Image URL", currentGear.getImageUrl());
                    intent.putExtra("Storage Path", currentGear.getStoragePath());

                    intent.putExtra("Item Provider Email", currentUser.getEmail());
                    intent.putExtra("Item Provider Name", currentUser.getName());
                    intent.putExtra("Item Provider Phone Number", currentUser.getPhoneNumber());
                    intent.putExtra("Item Provider Location", currentUser.getLocation());
                    intent.putExtra("Item Provider Picture", currentUser.getProfilePictureUrl());
                    intent.putExtra("Item Provider Token", currentUser.getToken());

                    intent.putExtra("Start Date", startDate);
                    intent.putExtra("End Date", endDate);
                    intent.putExtra("Total Days", totalDays);

                    startActivity(intent);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error! Not able to get data");
            }
        });
    }
}