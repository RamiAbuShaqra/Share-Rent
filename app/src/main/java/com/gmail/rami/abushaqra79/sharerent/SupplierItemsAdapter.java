package com.gmail.rami.abushaqra79.sharerent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class SupplierItemsAdapter extends ArrayAdapter<BabyGear> {

    public SupplierItemsAdapter(@NonNull Context context, int resource, @NonNull List<BabyGear> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if there is a recycled view to be used, otherwise create one
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.single_supplier_items_details, parent, false);
        }

        BabyGear currentBabyGear = getItem(position);

        TextView type = convertView.findViewById(R.id.item_type);
        type.setText(currentBabyGear.getBabyGearType());

        TextView description = convertView.findViewById(R.id.item_description);
        description.setText(currentBabyGear.getBabyGearDescription());

        TextView rentPrice = convertView.findViewById(R.id.rent_price);
        rentPrice.setText(currentBabyGear.getRentPrice());

        int numberOfDays = PreferenceActivity.CartPreferenceFragment.getNumberOfRentDays();
        TextView days = convertView.findViewById(R.id.number_of_days);
        days.setText(String.valueOf(numberOfDays));

        double finalPrice = Double.parseDouble(currentBabyGear.getRentPrice()) * numberOfDays;
        TextView totalPrice = convertView.findViewById(R.id.total_price);
        totalPrice.setText(String.valueOf(finalPrice));

        return convertView;
    }
}
