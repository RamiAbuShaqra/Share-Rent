package com.gmail.rami.abushaqra79.sharerent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.List;

public class BabyGearAdapter extends ArrayAdapter<BabyGear> {
    private final Context context;
    private final boolean isOrderSummary;

    public BabyGearAdapter(@NonNull Context context, int resource, @NonNull List<BabyGear> objects,
                           boolean isOrderSummary) {
        super(context, resource, objects);
        this.context = context;
        this.isOrderSummary = isOrderSummary;
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

        ImageView deleteItem = convertView.findViewById(R.id.delete_item);
        if (isOrderSummary) {
            deleteItem.setVisibility(View.VISIBLE);

            // setting OnClickListener to the delete imageview.
            // The trick is to call performItemClick and pass this view.
            // We will be able to catch this view in onItemClickListener’s onItemClick method.
            // According to the official doc, performItemClick method calls the OnItemClickListener
            // if it is defined.
            deleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Let the event be handled in onItemClick()
                    ((ListView) parent).performItemClick(v, position, 0);
                }
            });
        } else {
            deleteItem.setVisibility(View.GONE);
        }

        return convertView;
    }
}
