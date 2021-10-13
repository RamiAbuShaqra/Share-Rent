package com.gmail.rami.abushaqra79.sharerent;

import androidx.annotation.Nullable;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class SelectedItemActivity extends MainActivity {

    public static ArrayList<BabyGear> itemsToRent;

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

        Bundle bundle = getIntent().getExtras();
        String imageUrl = bundle.getString("Image URL");
        String description = bundle.getString("Description");
        String rentPrice = bundle.getString("Rent Price");
        String picture = bundle.getString("Item Provider Picture");
        String email = bundle.getString("Item Provider Email");
        String phone = bundle.getString("Item Provider Phone Number");

        ImageView photo = findViewById(R.id.selected_item_photo);
        TextView details = findViewById(R.id.selected_item_description);
        TextView price = findViewById(R.id.rent_price);
        ImageView userPicture = findViewById(R.id.item_provider_picture);
        TextView userEmail = findViewById(R.id.item_provider_email);
        TextView userPhoneNumber = findViewById(R.id.item_provider_phone);

        Glide.with(this).load(imageUrl).into(photo);
        details.setText(description);
        price.setText(rentPrice);

        Glide.with(this).load(picture).into(userPicture);
        userEmail.setText(email);
        userPhoneNumber.setText(phone);
    }
}