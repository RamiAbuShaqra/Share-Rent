package com.gmail.rami.abushaqra79.sharerent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class SelectedItemActivity extends AppCompatActivity {

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
        String description = bundle.getString("Description");
        String rentPrice = bundle.getString("Rent Price");
        String imageUrl = bundle.getString("Image URL");
        String phone = bundle.getString("Phone Number");

        ImageView photo = findViewById(R.id.selected_item_photo);
        TextView desc = findViewById(R.id.selected_item_description);
        TextView price = findViewById(R.id.rent_price);
        TextView phoneNumber = findViewById(R.id.item_provider_phone);

        Glide.with(this).load(imageUrl).into(photo);
        desc.setText(description);
        price.setText(rentPrice);
        phoneNumber.setText(phone);
    }
}