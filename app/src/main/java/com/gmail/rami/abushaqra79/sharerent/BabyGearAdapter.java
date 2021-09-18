package com.gmail.rami.abushaqra79.sharerent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class BabyGearAdapter extends ArrayAdapter<BabyGear> {
    public BabyGearAdapter(@NonNull Context context, int resource, @NonNull List<BabyGear> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.baby_gear_details,
                    parent,false);
        }

        BabyGear currentBabyGear = getItem(position);

        TextView babyGearType = convertView.findViewById(R.id.product_name);
        babyGearType.setText(currentBabyGear.getBabyGearType());

        TextView babyGearDescription = convertView.findViewById(R.id.product_description);
        babyGearDescription.setText(currentBabyGear.getBabyGearDescription());

        TextView rentPrice = convertView.findViewById(R.id.rent_price);
        rentPrice.setText(String.valueOf(currentBabyGear.getRentPrice()));

        ImageView babyGearPhoto = convertView.findViewById(R.id.product_photo);
        babyGearPhoto.setImageResource(currentBabyGear.getPhotoId());

        return convertView;
    }
}
