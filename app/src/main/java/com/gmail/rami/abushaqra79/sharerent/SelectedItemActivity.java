package com.gmail.rami.abushaqra79.sharerent;

import androidx.annotation.Nullable;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class SelectedItemActivity extends MainActivity {

    private ArrayList<BabyGear> itemsToRent;
    private ArrayList<User> itemsProviders;

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

        Vibrator vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

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
        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemsInShoppingCart = PreferenceActivity.CartPreferenceFragment.updateCart(1);
                cartTV.setText(String.valueOf(itemsInShoppingCart));

                itemsToRent = PreferenceActivity.CartPreferenceFragment.getSummaryOfItems();
                itemsToRent.add(new BabyGear(type, description, rentPrice, imageUrl, storagePath));
                PreferenceActivity.CartPreferenceFragment.addItemToPreference(itemsToRent);

                itemsProviders = PreferenceActivity.CartPreferenceFragment.getSummaryOfItemsProviders();
                itemsProviders.add(new User(email, name, phone, location, picture));
                PreferenceActivity.CartPreferenceFragment.addItemProviderToPreference(itemsProviders);

                vibrator.vibrate(100);

                Toast.makeText(SelectedItemActivity.this, "Added to Cart.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}