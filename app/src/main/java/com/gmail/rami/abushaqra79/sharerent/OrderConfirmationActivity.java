package com.gmail.rami.abushaqra79.sharerent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.FirebaseOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OrderConfirmationActivity extends AppCompatActivity {

    private static final String TAG = OrderConfirmationActivity.class.getSimpleName();
    private FirebaseFunctions functions;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private ArrayList<BabyGear> items;
    private ArrayList<User> users;
    private ArrayList<Order> orders;

    private String customerName;
    private String customerEmail;
    private String customerPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        functions = FirebaseFunctions.getInstance();

        db = FirebaseFirestore.getInstance();

        items = PreferenceActivity.CartPreferenceFragment.getSummaryOfItems();
        users = PreferenceActivity.CartPreferenceFragment.getSummaryOfItemsProviders();

        TextView confirm = findViewById(R.id.tttttttttt);
//        progressBar = findViewById(R.id.progress_bar);


        orders = PreferenceActivity.CartPreferenceFragment.getOrders();
        SeparateOrdersAdapter adapter = new SeparateOrdersAdapter(this, R.layout.separate_order_details, orders);
        ListView separateOrders = findViewById(R.id.separate_orders_list_view);
        separateOrders.setAdapter(adapter);






        completeCustomerInfo();







        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);

                //splitOrders();

                for (int i = 0; i < items.size(); i++) {
                    String supplierName = users.get(i).getName();
                    String supplierEmail = users.get(i).getEmail();
                    String itemType = items.get(i).getBabyGearType();
                    String itemDescription = items.get(i).getBabyGearDescription();
                    String itemRentPrice = items.get(i).getRentPrice();

                    sendEmail(supplierName, supplierEmail, itemType, itemDescription, itemRentPrice,
                            customerName, customerEmail, customerPhone);

//                    sendEmail(supplierName, supplierEmail, itemType, itemDescription, itemRentPrice,
//                            customerName, customerEmail, customerPhone)
//                            .addOnCompleteListener(new OnCompleteListener<String>() {
//                                @Override
//                                public void onComplete(@NonNull Task<String> task) {
//                                    if (!task.isSuccessful()) {
//                                        Exception e = task.getException();
//                                        Log.e(TAG, "sendEmail:onFailure", e);
//
//                                        progressBar.setVisibility(View.INVISIBLE);
//
//                                        Toast.makeText(OrderConfirmationActivity.this,
//                                                "An error occurred.\nPlease try again.",
//                                                Toast.LENGTH_LONG).show();
//                                        return;
//                                    }
//
//                                    progressBar.setVisibility(View.INVISIBLE);
//
//                                    AlertDialog.Builder builder = new AlertDialog.Builder(OrderConfirmationActivity.this);
//                                    builder.setMessage("The order was placed. The items provider " +
//                                            "will contact you soon for the delivery arrangements.\n" +
//                                            "\nThe payment will be 'cash on delivery' directly to the " +
//                                            "items provider.")
//                                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    int resetTheCart = PreferenceActivity.CartPreferenceFragment
//                                                            .updateCart(MainActivity.RESET_THE_CART);
//                                                    MainActivity.cartTV.setText(String.valueOf(resetTheCart));
//
//
//                                                    items.clear();
//                                                    PreferenceActivity.CartPreferenceFragment.addItemToPreference(items);
//
//
//                                                    users.clear();
//                                                    PreferenceActivity.CartPreferenceFragment.addItemProviderToPreference(users);
//
//                                                    Intent intent = new Intent(OrderConfirmationActivity.this, MainActivity.class);
//                                                    startActivity(intent);
//
//                                                    if (dialog != null) {
//                                                        dialog.dismiss();
//                                                    }
//                                                }
//                                            });
//
//                                    AlertDialog alertDialog = builder.create();
//                                    alertDialog.show();
//                                }
//                            });
                }

            }
        });
    }

//    private Task<String> sendEmail(String supplierName, String supplierEmail,
//                                   String itemType, String itemDescription, String itemRentPrice,
//                                   String customerName, String customerEmail, String customerPhone) {
//        Map<String, Object> data = new HashMap<>();
//        data.put("Name", "Omar Osama");
//        data.put("iiemail", "rami.abushaqra79@gmail.com");
//        data.put("Item Type", itemType);
//        data.put("Item Description", itemDescription);
//        data.put("Item Rent Price", itemRentPrice);
//        data.put("Customer Name", customerName);
//        data.put("Customer Email", customerEmail);
//        data.put("Customer Phone Number", customerPhone);
//
//        return functions
//                .getHttpsCallable("sendEmail")
//                .call(data)
//                .continueWith(new Continuation<HttpsCallableResult, String>() {
//                    @Override
//                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
//                        return (String) task.getResult().getData();
//                    }
//                });
//    }

    private void sendEmail(String supplierName, String supplierEmail,
                           String itemType, String itemDescription, String itemRentPrice,
                           String customerName, String customerEmail, String customerPhone) {
        Map<String, Object> mail = new HashMap<>();
        mail.put("to", supplierEmail);
        Map<String, Object> message = new HashMap<>();
        message.put("subject", "Share Rent - New Order");
        //message.put("text", "This is the plaintext section of the email body.");
        message.put("html", "Hi " + supplierName + "," +
                "<br><br>You have a new order for the below item(s):" +
                "<br>" + itemType + " - " + itemDescription +
                "<br><br>Below is the customer information:" +
                "<br>Name: " + customerName +
                "<br>E-mail: " + customerEmail +
                "<br>Phone number: " + customerPhone +
                "<br><br>Please contact the customer on his/her contact details for the delivery arrangements." +
                "<br><br><br>Have a nice day." +
                "<br><br>Share Rent Admin");

        mail.put("message", message);
        db.collection("email_collection").add(mail).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Exception e = task.getException();
                Log.e(TAG, "sendEmail:onFailure", e);

                progressBar.setVisibility(View.INVISIBLE);

                Toast.makeText(OrderConfirmationActivity.this,
                        "An error occurred.\nPlease try again.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            progressBar.setVisibility(View.INVISIBLE);

            AlertDialog.Builder builder = new AlertDialog.Builder(OrderConfirmationActivity.this);
            builder.setMessage("The order was placed. The items provider " +
                    "will contact you soon for the delivery arrangements.\n" +
                    "\nThe payment will be 'cash on delivery' directly to the " +
                    "items provider.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            int resetTheCart = PreferenceActivity.CartPreferenceFragment
//                                    .updateCart(MainActivity.RESET_THE_CART);
//                            MainActivity.cartTV.setText(String.valueOf(resetTheCart));
//
//
//                            items.clear();
//                            PreferenceActivity.CartPreferenceFragment.addItemToPreference(items);
//
//
//                            users.clear();
//                            PreferenceActivity.CartPreferenceFragment.addItemProviderToPreference(users);
//
//                            Intent intent = new Intent(OrderConfirmationActivity.this, MainActivity.class);
//                            startActivity(intent);

                            if (dialog != null) {
                                dialog.dismiss();
                            }
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }





    private void completeCustomerInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.customer_information_dialog, null);
        builder.setView(dialogView)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    wantToCloseDialog = true;
                }

                if (wantToCloseDialog) {
                    alertDialog.dismiss();
                }
            }
        });
    }





    private void splitOrders() {
        ArrayList<BabyGear> stuff = items;
        ArrayList<User> suppliers = users;
        ArrayList<Integer> index = new ArrayList<>();
        ArrayList<Map> maps = new ArrayList<>();

        while (suppliers.size() != 0) {
            Map<Object, Object> map = new HashMap<>();
            map.put(suppliers.get(0), stuff.get(0));

            index.add(0);

            for (int i = 0; i < suppliers.size(); i++) {
                if (i + 1 == suppliers.size()) {
                    break;
                } else if (suppliers.get(0).getEmail().equals(suppliers.get(i + 1).getEmail())) {
                    map.put(suppliers.get(0), stuff.get(i + 1));

                    index.add(i + 1);
                }
            }

            for (int j = 0; j < index.size(); j++) {
                if (index.get(j) != null) {
                    stuff.remove(index.get(j));
                    suppliers.remove(index.get(j));
                }

                index.clear();
                maps.add(map);
            }
        }

        Log.e(TAG, "------------ " + maps.toString());
    }
}