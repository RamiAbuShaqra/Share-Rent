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

import java.util.ArrayList;
import java.util.List;

/**
 * Custom adapter to display baby gear items that were added to cart.
 */
public class CartItemsAdapter extends ArrayAdapter<BabyGear> {

    private final Context context;
    private final ArrayList<BookingDates> dates;

    public CartItemsAdapter(@NonNull Context context, int resource, @NonNull List<BabyGear> objects,
                            ArrayList<BookingDates> dates) {
        super(context, resource, objects);
        this.context = context;
        this.dates = dates;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if there is a recycled view to be used, otherwise create one
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cart_items_details,
                    parent, false);
        }

        BabyGear currentBabyGear = getItem(position);

        TextView babyGearType = convertView.findViewById(R.id.item_type);
        babyGearType.setText(currentBabyGear.getBabyGearType());

        TextView babyGearDescription = convertView.findViewById(R.id.item_description);
        babyGearDescription.setText(currentBabyGear.getBabyGearDescription());

        TextView rentPrice = convertView.findViewById(R.id.rent_price);
        rentPrice.setText(currentBabyGear.getRentPrice());

        TextView numberOfDays = convertView.findViewById(R.id.number_of_days);
        numberOfDays.setText(String.valueOf(dates.get(position).getTotalDays()));

        double finalPrice = Integer.parseInt(currentBabyGear.getRentPrice())
                * dates.get(position).getTotalDays();

        TextView totalPrice = convertView.findViewById(R.id.total_price);
        totalPrice.setText(String.valueOf(finalPrice));

        ImageView babyGearPhoto = convertView.findViewById(R.id.item_photo);
        String imageUrl = currentBabyGear.getImageUrl();

        // Setting the item photo to ImageView using Glide library.
        Glide.with(context).load(imageUrl).into(babyGearPhoto);

        // Trash to remove the item from shopping cart.
        ImageView deleteItem = convertView.findViewById(R.id.delete_item);

        // setting OnClickListener to the delete imageview.
        // The trick is to call performItemClick and pass this view.
        // We will be able to catch this view in onItemClickListener’s onItemClick method.
        // According to the official doc, performItemClick method calls the OnItemClickListener
        // if it is defined.
        deleteItem.setOnClickListener(v -> {
            // Let the event be handled in onItemClick()
            ((ListView) parent).performItemClick(v, position, 0);
        });

        return convertView;
    }
}
