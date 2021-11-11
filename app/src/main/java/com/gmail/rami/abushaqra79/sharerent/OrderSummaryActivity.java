package com.gmail.rami.abushaqra79.sharerent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class OrderSummaryActivity extends AppCompatActivity {

    private static final String TAG = OrderSummaryActivity.class.getSimpleName();
    private ArrayList<BabyGear> items;
    private ArrayList<User> users;
    private ArrayList<Order> orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

        items = PreferenceActivity.CartPreferenceFragment.getSummaryOfItems();

        TextView totalQuantity = findViewById(R.id.total_quantity_of_items);
        totalQuantity.setText(String.valueOf(items.size()));

        LinearLayout daysSummary = findViewById(R.id.days_summary);
        if (items.size() == 0) {
            daysSummary.setVisibility(View.GONE);
        }

        double rentPrice = 0;
        for (int i = 0; i < items.size(); i++) {
            rentPrice += Double.parseDouble(items.get(i).getRentPrice());
        }

        TextView totalPrice = findViewById(R.id.total_price);
        totalPrice.setText(String.valueOf(rentPrice));

        int numberOfDays = PreferenceActivity.CartPreferenceFragment.getNumberOfRentDays();

        TextView days = findViewById(R.id.number_of_days);
        days.setText(String.valueOf(numberOfDays));

        double finalPrice = rentPrice * numberOfDays;

        TextView finalTotalPrice = findViewById(R.id.final_total_price);
        finalTotalPrice.setText(String.valueOf(finalPrice));

        BabyGearAdapter adapter = new BabyGearAdapter(this, R.layout.baby_gear_details,
                items, true);

        ListView orderSummaryListView = findViewById(R.id.order_summary_list_view);
        orderSummaryListView.setAdapter(adapter);

        orderSummaryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BabyGear deletedItem = adapter.getItem(position);
                long viewId = view.getId();

                if (viewId == R.id.delete_item) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(OrderSummaryActivity.this);
                    builder.setMessage("Remove item from cart?")
                            .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
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
                                    for (int i = 0; i < orders.size(); i++) {
                                        ArrayList<BabyGear> list = orders.get(i).getListItems();
                                        for (int j = 0; j < list.size(); j++) {
                                            if (list.get(j).getBabyGearType().equals(deletedItem.getBabyGearType())
                                                    && list.get(j).getBabyGearDescription()
                                                            .equals(deletedItem.getBabyGearDescription())) {
                                                index = i;
                                                list.remove(j);
                                                gears = list;
                                                break;
                                            }
                                        }
                                    }

                                    if (gears.size() == 0) {
                                        orders.remove(index);
                                    } else {
                                        Order order = new Order(email, name, phone, picture, token, gears);
                                        orders.set(index, order);
                                    }
                                    PreferenceActivity.CartPreferenceFragment.addOrder(orders);

                                    int shoppingCart = PreferenceActivity.CartPreferenceFragment
                                            .updateCart(-1);
                                    MainActivity.cartTV.setText(String.valueOf(shoppingCart));

                                    updateSummaryActivity();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (dialog != null) {
                                        dialog.dismiss();
                                    }
                                }
                            });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });

        LinearLayout makeOrder = findViewById(R.id.make_the_order);
        makeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (items.size() == 0) {
                    Toast.makeText(OrderSummaryActivity.this,
                            "There are no items in the shopping cart !!", Toast.LENGTH_LONG).show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(OrderSummaryActivity.this);
                builder.setMessage("Are you sure you want to proceed to rent these items?\n" +
                        "\nSelect 'Yes' to place an order to suppliers, or select 'No' to cancel.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(OrderSummaryActivity.this,
                                        OrderConfirmationActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

    private void updateSummaryActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}