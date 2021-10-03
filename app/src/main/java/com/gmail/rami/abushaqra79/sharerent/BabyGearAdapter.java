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

import com.bumptech.glide.Glide;

import java.util.List;

public class BabyGearAdapter extends ArrayAdapter<BabyGear> {
    private final Context context;

    public BabyGearAdapter(@NonNull Context context, int resource, @NonNull List<BabyGear> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.baby_gear_details,
                    parent,false);
        }

        BabyGear currentBabyGear = getItem(position);

        TextView babyGearType = convertView.findViewById(R.id.item_type);
        babyGearType.setText(currentBabyGear.getBabyGearType());

        TextView babyGearDescription = convertView.findViewById(R.id.item_description);
        babyGearDescription.setText(currentBabyGear.getBabyGearDescription());

        TextView rentPrice = convertView.findViewById(R.id.rent_price);
        rentPrice.setText(currentBabyGear.getRentPrice());

        ImageView babyGearPhoto = convertView.findViewById(R.id.item_photo);
        String imageUrl = currentBabyGear.getImageUrl();

        Glide.with(context).load(imageUrl).into(babyGearPhoto);

        return convertView;
    }
}
