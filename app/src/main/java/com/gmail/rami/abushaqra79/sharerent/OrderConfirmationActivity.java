package com.gmail.rami.abushaqra79.sharerent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The activity in which the customer can review his orders and place them separately to each
 * supplier.
 */
public class OrderConfirmationActivity extends AppCompatActivity {

    /**
     * Tag for the log messages
     */
    private static final String TAG = OrderConfirmationActivity.class.getSimpleName();

    private FirebaseFirestore db;
    private FirebaseFunctions functions;

    private ArrayList<BookingDates> dates;
    private ArrayList<BabyGear> items;
    private ArrayList<User> users;
    private ArrayList<Order> orders;

    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        // Get instance of Firebase Firestore database
        db = FirebaseFirestore.getInstance();
        // Get instance of Firebase Functions
        functions = FirebaseFunctions.getInstance();

        dates = PreferenceActivity.CartPreferenceFragment.getBookingDates();
        items = PreferenceActivity.CartPreferenceFragment.getSummaryOfItems();
        users = PreferenceActivity.CartPreferenceFragment.getSummaryOfItemsProviders();
        orders = PreferenceActivity.CartPreferenceFragment.getOrders();

        progressBar = findViewById(R.id.progress_bar);

        // Adapter for displaying the list of separate orders
        SeparateOrdersAdapter adapter = new SeparateOrdersAdapter(this,
                R.layout.separate_order_details, orders, dates);
        ListView separateOrders = findViewById(R.id.separate_orders_list_view);
        separateOrders.setAdapter(adapter);

        separateOrders.setOnItemClickListener((parent, view, position, id) -> {
            Order currentOrder = adapter.getItem(position);

            long viewId = view.getId();

            if (viewId == R.id.place_order) {
                if (TextUtils.isEmpty(customerName) && TextUtils.isEmpty(customerEmail) &&
                        TextUtils.isEmpty(customerPhone)) {
                    completeCustomerInfo(currentOrder);
                } else {
                    progressBar.setVisibility(View.VISIBLE);

                    sendEmail(currentOrder);
                }
            }
        });
    }

    /**
     * Sends e-mail to the supplier when the customer placed an order for any of his/her items.
     *
     * @param order The order which contains the list of items to be rented from a single supplier.
     */
    private void sendEmail(Order order) {
        String supplierEmail = order.getSupplierEmail();
        String supplierName = order.getSupplierName();
        String token = order.getSupplierToken();
        ArrayList<BabyGear> list = order.getListItems();

        int totalPrice = 0;
        // Building the list details to be included in the email message
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            text.append(i + 1).append(") ").append(list.get(i).getBabyGearType()).append(" - ")
                    .append(list.get(i).getBabyGearDescription()).append(".").append("<br>")
                    .append("From: ").append(dates.get(i).getStartDate()).append(" to ")
                    .append(dates.get(i).getEndDate()).append(". Rent price is: ")
                    .append(Double.parseDouble(list.get(i).getRentPrice()) * dates.get(i).getTotalDays())
                    .append(" $").append("<br>");

            totalPrice += Double.parseDouble(list.get(i).getRentPrice()) * dates.get(i).getTotalDays();
        }

        text.append("<br>Total price: ").append(totalPrice).append("<br><br>");
        String itemDetails = text.toString();

        Map<String, Object> mail = new HashMap<>();
        mail.put("to", supplierEmail);
        Map<String, Object> message = new HashMap<>();
        message.put("subject", "Share Rent - New Order");
        message.put("html", "Hi " + supplierName + "," +
                "<br><br>You have a new order for the below item(s):" +
                "<br>" + itemDetails +
                "<br><br>Below is the customer information:" +
                "<br>Name: " + customerName +
                "<br>E-mail: " + customerEmail +
                "<br>Phone number: " + customerPhone +
                "<br><br>Please contact the customer on his/her contact details for the delivery arrangements." +
                "<br><br><br>Have a nice day." +
                "<br><br>Share Rent Admin");

        // Use Firebase Functions to send the email as a task
        mail.put("message", message);
        db.collection("email_collection").add(mail).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Exception e = task.getException();
                Log.e(TAG, "sendEmail:onFailure", e);

                progressBar.setVisibility(View.INVISIBLE);

                Toast.makeText(OrderConfirmationActivity.this,
                        "An error occurred.\nPlease try again.", Toast.LENGTH_LONG).show();
                return;
            }

            progressBar.setVisibility(View.INVISIBLE);

            // Create an alert dialog to notify the user (customer) if the order is placed
            // successfully or not
            AlertDialog.Builder builder = new AlertDialog.Builder(OrderConfirmationActivity.this);
            builder.setMessage("The order was placed. The items provider " +
                    "will contact you soon for the delivery arrangements.\n" +
                    "\nThe payment will be 'cash on delivery' directly to the " +
                    "items provider.")
                    .setPositiveButton("Ok", (dialog, which) -> {
                        ArrayList<Integer> indices = new ArrayList<>();

                        for (int i = 0; i < order.getListItems().size(); i++) {
                            int index = checkForMatch(order.getListItems().get(i));
                            if (index != -1) {
                                indices.add(index);
                            }
                        }

                        // Clear the details of rented items (booking details, items list,
                        // supplier details, etc..) from SharedPreferences
                        for (int i = indices.size() - 1; i >= 0; i--) {
                            dates.remove((int) indices.get(i));
                            items.remove((int) indices.get(i));
                            users.remove((int) indices.get(i));
                        }

                        orders.remove(order);

                        int shoppingCart = PreferenceActivity.CartPreferenceFragment
                                .updateCart(indices.size() * -1);
                        MainActivity.cartTV.setText(String.valueOf(shoppingCart));

                        PreferenceActivity.CartPreferenceFragment.addBookingDates(dates);
                        PreferenceActivity.CartPreferenceFragment.addItemToPreference(items);
                        PreferenceActivity.CartPreferenceFragment.addItemProviderToPreference(users);
                        PreferenceActivity.CartPreferenceFragment.addOrder(orders);

                        sendToken(token);

                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }

    /**
     * Creates an alert dialog so the customer can enter his/her contact details to be included
     * in the email message that will be sent to supplier.
     *
     * @param order The order which contains the list of items to be rented from a single supplier.
     */
    private void completeCustomerInfo(Order order) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.customer_information_dialog, null);
        builder.setView(dialogView)
                .setPositiveButton("Confirm", (dialog, which) -> {
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            boolean wantToCloseDialog = false;

            TextInputLayout nameLayout = dialogView.findViewById(R.id.name_input_layout);
            TextInputLayout emailLayout = dialogView.findViewById(R.id.email_input_layout);
            TextInputLayout phoneLayout = dialogView.findViewById(R.id.phone_input_layout);
            EditText name = dialogView.findViewById(R.id.your_name);
            EditText email = dialogView.findViewById(R.id.your_email);
            EditText phone = dialogView.findViewById(R.id.your_phone);

            name.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    nameLayout.setError(null);
                }
            });

            email.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    emailLayout.setError(null);
                }
            });

            phone.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    phoneLayout.setError(null);
                }
            });

            customerName = name.getText().toString().trim();
            customerEmail = email.getText().toString().trim();
            customerPhone = phone.getText().toString().trim();

            if (TextUtils.isEmpty(customerName)) {
                nameLayout.setError("Enter name");
            }

            if (TextUtils.isEmpty(customerEmail) ||
                    !Patterns.EMAIL_ADDRESS.matcher(customerEmail).matches()) {
                emailLayout.setError("Enter a valid e-mail address");
            }

            if (TextUtils.isEmpty(customerPhone)) {
                phoneLayout.setError("Enter phone number");
            }

            if (!TextUtils.isEmpty(customerName) && !TextUtils.isEmpty(customerEmail) &&
                    Patterns.EMAIL_ADDRESS.matcher(customerEmail).matches() &&
                    !TextUtils.isEmpty(customerPhone)) {
                sendEmail(order);
                progressBar.setVisibility(View.VISIBLE);
                wantToCloseDialog = true;
            }

            if (wantToCloseDialog) {
                alertDialog.dismiss();
            }
        });
    }

    /**
     * This helper method looks for the rented baby gears from the items ArrayList so it can be
     * removed from the list in the SharedPreferences (because the order is already placed to
     * the supplier to rent those items).
     *
     * @param babyGear The rented baby gear item.
     * @return The index of matched baby gear in items ArrayList.
     */
    private int checkForMatch(BabyGear babyGear) {
        int index = -1;

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getBabyGearDescription().equals(babyGear.getBabyGearDescription()) &&
                    items.get(i).getBabyGearType().equals(babyGear.getBabyGearType()) &&
                    items.get(i).getRentPrice().equals(babyGear.getRentPrice()) &&
                    items.get(i).getImageUrl().equals(babyGear.getImageUrl()) &&
                    items.get(i).getStoragePath().equals(babyGear.getStoragePath())) {
                index = i;
            }
        }

        return index;
    }

    /**
     * This helper method is used to send the device registered token to the Node.js server which
     * is integrated with Firebase Cloud Callable Function, to send a message notification to
     * supplier once the order is placed.
     *
     * @param token is the registered token for the device.
     */
    private void sendToken(String token) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);

        functions
                .getHttpsCallable("sendToken")
                .call(data)
                .continueWith(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return (String) task.getResult().getData();
                });
    }
}