package com.gmail.rami.abushaqra79.sharerent;

import androidx.annotation.Nullable;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * This activity is to review all details of a selected item with its supplier information.
 */
public class SelectedItemActivity extends MainActivity {

    private ArrayList<BookingDates> dates;
    private ArrayList<BabyGear> itemsToRent;
    private ArrayList<User> itemsProviders;
    private ArrayList<Order> orders;

    /**
     * Obtain the previous instance of ReviewItemsOptions.java from the back stack
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
        setContentView(R.layout.activity_selected_item);

        // Vibrator object for making a vibration notification when an item is added to cart
        Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        // Get intent with its extras
        Bundle bundle = getIntent().getExtras();

        String type = bundle.getString("Type");
        String description = bundle.getString("Description");
        String rentPrice = bundle.getString("Rent Price");
        String imageUrl = bundle.getString("Image URL");
        String storagePath = bundle.getString("Storage Path");

        String email = bundle.getString("Item Provider Email");
        String name = bundle.getString("Item Provider Name");
        String phone = bundle.getString("Item Provider Phone Number");
        String location = bundle.getString("Item Provider Location");
        String picture = bundle.getString("Item Provider Picture");
        String token = bundle.getString("Item Provider Token");

        String startDate = bundle.getString("Start Date");
        String endDate = bundle.getString("End Date");
        int totalDays = bundle.getInt("Total Days");

        ImageView photo = findViewById(R.id.selected_item_photo);
        TextView details = findViewById(R.id.selected_item_description);
        TextView price = findViewById(R.id.rent_price);
        ImageView userPicture = findViewById(R.id.item_provider_picture);
        TextView userName = findViewById(R.id.item_provider_name);
        TextView userEmail = findViewById(R.id.item_provider_email);
        TextView userPhoneNumber = findViewById(R.id.item_provider_phone);

        Glide.with(this).load(imageUrl).into(photo);
        details.setText(description);
        price.setText(rentPrice);

        Glide.with(this).load(picture).into(userPicture);
        userName.setText(name);
        userEmail.setText(email);
        userPhoneNumber.setText(phone);

        FrameLayout addToCart = findViewById(R.id.add_to_cart_view);
        addToCart.setOnClickListener(v -> {
            // Update all information in SharedPreferences (items, booking dates, suppliers, etc..)
            int itemsInShoppingCart = PreferenceActivity.CartPreferenceFragment.updateCart(1);
            cartTV.setText(String.valueOf(itemsInShoppingCart));

            dates = PreferenceActivity.CartPreferenceFragment.getBookingDates();
            dates.add(new BookingDates(startDate, endDate, totalDays));
            PreferenceActivity.CartPreferenceFragment.addBookingDates(dates);

            itemsToRent = PreferenceActivity.CartPreferenceFragment.getSummaryOfItems();
            itemsToRent.add(new BabyGear(type, description, rentPrice, imageUrl, storagePath));
            PreferenceActivity.CartPreferenceFragment.addItemToPreference(itemsToRent);

            itemsProviders = PreferenceActivity.CartPreferenceFragment.getSummaryOfItemsProviders();
            itemsProviders.add(new User(email, name, phone, location, picture, token));
            PreferenceActivity.CartPreferenceFragment.addItemProviderToPreference(itemsProviders);

            orders = PreferenceActivity.CartPreferenceFragment.getOrders();
            if (orders.size() > 0) {
                boolean newSupplier = true;
                for (int i = 0; i < orders.size(); i++) {
                    if (orders.get(i).getSupplierEmail().equals(email)) {
                        orders.get(i).getListItems().add(
                                new BabyGear(type, description, rentPrice, imageUrl, storagePath));
                        orders.get(i).getDatesDetails().add(
                                new BookingDates(startDate, endDate, totalDays));

                        newSupplier = false;
                    }
                }
                if (newSupplier) {
                    ArrayList<BabyGear> gears = new ArrayList<>();
                    gears.add(new BabyGear(type, description, rentPrice, imageUrl, storagePath));

                    ArrayList<BookingDates> dates = new ArrayList<>();
                    dates.add(new BookingDates(startDate, endDate, totalDays));

                    orders.add(new Order(email, name, phone, picture, token, gears, dates));
                }
            } else {
                ArrayList<BabyGear> gears = new ArrayList<>();
                gears.add(new BabyGear(type, description, rentPrice, imageUrl, storagePath));

                ArrayList<BookingDates> dates = new ArrayList<>();
                dates.add(new BookingDates(startDate, endDate, totalDays));

                orders.add(new Order(email, name, phone, picture, token, gears, dates));
            }

            PreferenceActivity.CartPreferenceFragment.addOrder(orders);

            vibrator.vibrate(100);

            Toast.makeText(SelectedItemActivity.this, "Added to Cart.",
                    Toast.LENGTH_LONG).show();
        });
    }
}