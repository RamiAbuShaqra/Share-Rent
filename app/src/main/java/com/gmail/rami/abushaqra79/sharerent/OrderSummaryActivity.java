package com.gmail.rami.abushaqra79.sharerent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * This activity is where the customer can review all the items that were added to cart so he/she
 * can proceed to place orders or delete some items.
 */
public class OrderSummaryActivity extends AppCompatActivity {

    private ArrayList<BookingDates> dates;
    private ArrayList<BabyGear> items;
    private ArrayList<User> users;
    private ArrayList<Order> orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

        // Get the items details from SharedPreferences
        dates = PreferenceActivity.CartPreferenceFragment.getBookingDates();
        items = PreferenceActivity.CartPreferenceFragment.getSummaryOfItems();

        TextView totalQuantity = findViewById(R.id.total_quantity_of_items);
        totalQuantity.setText(String.valueOf(items.size()));

        double rentPrice = 0;
        for (int i = 0; i < items.size(); i++) {
            rentPrice += (Double.parseDouble(items.get(i).getRentPrice()) * dates.get(i).getTotalDays());
        }

        TextView totalPrice = findViewById(R.id.total_price);
        totalPrice.setText(String.valueOf(rentPrice));

        // Adapter used to display list of items in the shopping cart
        CartItemsAdapter adapter = new CartItemsAdapter(this, R.layout.cart_items_details,
                items, dates);

        ListView orderSummaryListView = findViewById(R.id.order_summary_list_view);
        orderSummaryListView.setAdapter(adapter);

        orderSummaryListView.setOnItemClickListener((parent, view, position, id) -> {
            BabyGear deletedItem = adapter.getItem(position);

            long viewId = view.getId();

            // If the customer deletes a specific item from the cart,
            // it will deleted from SharedPreferences
            if (viewId == R.id.delete_item) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OrderSummaryActivity.this);
                builder.setMessage("Remove item from cart?")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            dates = PreferenceActivity.CartPreferenceFragment.getBookingDates();
                            dates.remove(position);
                            PreferenceActivity.CartPreferenceFragment.addBookingDates(dates);

                            items = PreferenceActivity.CartPreferenceFragment.getSummaryOfItems();
                            items.remove(position);
                            PreferenceActivity.CartPreferenceFragment.addItemToPreference(items);

                            users = PreferenceActivity.CartPreferenceFragment.getSummaryOfItemsProviders();
                            String email = users.get(position).getEmail();
                            String name = users.get(position).getName();
                            String phone = users.get(position).getPhoneNumber();
                            String picture = users.get(position).getProfilePictureUrl();
                            String token = users.get(position).getToken();
                            users.remove(position);
                            PreferenceActivity.CartPreferenceFragment.addItemProviderToPreference(users);

                            orders = PreferenceActivity.CartPreferenceFragment.getOrders();
                            int index = 0;
                            ArrayList<BabyGear> gears = new ArrayList<>();
                            ArrayList<BookingDates> days = new ArrayList<>();
                            for (int i = 0; i < orders.size(); i++) {
                                ArrayList<BabyGear> list = orders.get(i).getListItems();
                                ArrayList<BookingDates> bookingDates = orders.get(i).getDatesDetails();
                                for (int j = 0; j < list.size(); j++) {
                                    if (list.get(j).getBabyGearType().equals(deletedItem.getBabyGearType())
                                            && list.get(j).getBabyGearDescription()
                                                    .equals(deletedItem.getBabyGearDescription())) {
                                        index = i;
                                        list.remove(j);
                                        bookingDates.remove(j);
                                        gears = list;
                                        days = bookingDates;
                                        break;
                                    }
                                }
                            }

                            if (gears.size() == 0) {
                                orders.remove(index);
                            } else {
                                Order order = new Order(email, name, phone, picture, token, gears, days);
                                orders.set(index, order);
                            }
                            PreferenceActivity.CartPreferenceFragment.addOrder(orders);

                            int shoppingCart = PreferenceActivity.CartPreferenceFragment
                                    .updateCart(-1);
                            MainActivity.cartTV.setText(String.valueOf(shoppingCart));

                            updateSummaryActivity();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        LinearLayout makeOrder = findViewById(R.id.make_the_order);
        makeOrder.setOnClickListener(v -> {
            if (items.size() == 0) {
                Toast.makeText(OrderSummaryActivity.this,
                        "There are no items in the shopping cart !!", Toast.LENGTH_LONG).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(OrderSummaryActivity.this);
            builder.setMessage("Are you sure you want to proceed to rent these items?\n" +
                    "\nSelect 'Yes' to place an order to suppliers, or select 'No' to cancel.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Intent intent = new Intent(OrderSummaryActivity.this,
                                OrderConfirmationActivity.class);
                        startActivity(intent);
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }

    /**
     * Updates and refreshes the activity after deleting any item.
     */
    private void updateSummaryActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}