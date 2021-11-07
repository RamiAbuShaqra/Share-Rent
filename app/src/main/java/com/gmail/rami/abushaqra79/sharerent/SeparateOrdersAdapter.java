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

public class SeparateOrdersAdapter extends ArrayAdapter<Order> {
    private final Context context;

    public SeparateOrdersAdapter(@NonNull Context context, int resource, @NonNull List<Order> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if there is a recycled view to be used, otherwise create one
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.separate_order_details,
                    parent, false);
        }

        Order currentOrder = getItem(position);

        ImageView userPicture = convertView.findViewById(R.id.item_provider_picture);
        String photoUrl = currentOrder.getPhotoUrl();

        // Setting the item photo to ImageView using Glide library
        Glide.with(context).load(photoUrl).into(userPicture);

        TextView supplierName = convertView.findViewById(R.id.item_provider_name);
        supplierName.setText(currentOrder.getSupplierName());

        TextView supplierEmail = convertView.findViewById(R.id.item_provider_email);
        supplierEmail.setText(currentOrder.getSupplierEmail());

        TextView supplierPhoneNumber = convertView.findViewById(R.id.item_provider_phone);
        supplierPhoneNumber.setText(currentOrder.getPhoneNumber());

        SupplierItemsAdapter adapter = new SupplierItemsAdapter(context,
                R.layout.single_supplier_items_details, currentOrder.getListItems());
        ListView itemsList = convertView.findViewById(R.id.items_for_single_supplier);
        itemsList.getLayoutParams().height = currentOrder.getListItems().size() * 300;
        itemsList.setAdapter(adapter);

        double total = 0;
        int numberOfDays = PreferenceActivity.CartPreferenceFragment.getNumberOfRentDays();

        for (int i = 0; i < currentOrder.getListItems().size(); i++) {
            total += Integer.parseInt(currentOrder.getListItems().get(i).getRentPrice());
        }

        TextView totalPrice = convertView.findViewById(R.id.total_price);
        totalPrice.setText(String.valueOf(total * numberOfDays));

        TextView placeOrder = convertView.findViewById(R.id.place_order);

        // setting OnClickListener to the placeOrder textview.
        // The trick is to call performItemClick and pass this view.
        // We will be able to catch this view in onItemClickListener’s onItemClick method.
        // According to the official doc, performItemClick method calls the OnItemClickListener
        // if it is defined.
        placeOrder.setOnClickListener(v -> {
            // Let the event be handled in onItemClick()
            ((ListView) parent).performItemClick(v, position, 0);
        });

        return convertView;
    }
}
