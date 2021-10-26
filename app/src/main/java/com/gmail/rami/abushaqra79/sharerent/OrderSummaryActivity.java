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

    private final String MOBILE_NUMBER = "+962795828385";
    private String message;

    private ArrayList<BabyGear> items;
    private ArrayList<User> users;

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
                                    users.remove(position);
                                    PreferenceActivity.CartPreferenceFragment.addItemProviderToPreference(users);

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
                                test();
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


//                int totalItemsQuantity = 0;
//                SelectedItemActivity.itemsToRent = PreferenceActivity.CartPreferenceFragment.getSummaryOfItems();
//
//                StringBuilder builder = new StringBuilder();
//                builder = builder.append("Hi..").append("\nI want to make the below order:\n");
//
//                for (int i = 0; i < SelectedItemActivity.itemsToRent.size(); i++) {
//                    builder = builder.append("\nPerfume # ").append(i + 1).append(" ------ Quantity: ")
//                            .append(SelectedItemActivity.itemsToRent.get(i));
//
//                    //totalItemsQuantity += SelectedItemActivity.itemsToRent.get(i);
//                }
//
//                message = builder.toString();
//
//                int resetTheCart = PreferenceActivity.CartPreferenceFragment.updateCart(MainActivity.RESET_THE_CART);
//                MainActivity.cartTV.setText(String.valueOf(resetTheCart));
//
//                SelectedItemActivity.itemsToRent.clear();
//                PreferenceActivity.CartPreferenceFragment.addItemToPreference(SelectedItemActivity.itemsToRent);
//
//                updateSummaryActivity();
//
//                    String url = null;
//                    try {
//                        url = "https://api.whatsapp.com/send?phone=" + MOBILE_NUMBER + "&text=" + URLEncoder.encode(message, "UTF-8");
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
//                    Intent sendIntent = new Intent(Intent.ACTION_VIEW);
//                    sendIntent.setData(Uri.parse(url));
//                    startActivity(sendIntent);
//                }
            }
        });
    }

    private void updateSummaryActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void test() {
        Toast.makeText(OrderSummaryActivity.this, "this is test", Toast.LENGTH_LONG).show();
    }
}