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
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderConfirmationActivity extends AppCompatActivity {

    private static final String TAG = OrderConfirmationActivity.class.getSimpleName();
    private FirebaseFunctions functions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        functions = FirebaseFunctions.getInstance();

        TextInputLayout nameLayout = findViewById(R.id.name_input_layout);
        TextInputLayout emailLayout = findViewById(R.id.email_input_layout);
        TextInputLayout phoneLayout = findViewById(R.id.phone_input_layout);
        EditText name = findViewById(R.id.your_name);
        EditText email = findViewById(R.id.your_email);
        EditText phone = findViewById(R.id.your_phone);
        TextView confirm = findViewById(R.id.confirm);
        ProgressBar progressBar = findViewById(R.id.progress_bar);

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

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredName = name.getText().toString().trim();
                String enteredEmail = email.getText().toString().trim();
                String enteredPhone = phone.getText().toString().trim();

                if (TextUtils.isEmpty(enteredName)) {
                    nameLayout.setError("Enter name");
                    return;
                }

                if (TextUtils.isEmpty(enteredEmail) ||
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches()) {
                    emailLayout.setError("Enter a valid e-mail address");
                    return;
                }

                if (TextUtils.isEmpty(enteredPhone)) {
                    phoneLayout.setError("Enter phone number");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                sendEmail()
                        .addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
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
                                                int resetTheCart = PreferenceActivity.CartPreferenceFragment
                                                        .updateCart(MainActivity.RESET_THE_CART);
                                                MainActivity.cartTV.setText(String.valueOf(resetTheCart));

                                                ArrayList<BabyGear> items = PreferenceActivity.CartPreferenceFragment.getSummaryOfItems();
                                                items.clear();
                                                PreferenceActivity.CartPreferenceFragment.addItemToPreference(items);

                                                ArrayList<User> users = PreferenceActivity.CartPreferenceFragment.getSummaryOfItemsProviders();
                                                users.clear();
                                                PreferenceActivity.CartPreferenceFragment.addItemProviderToPreference(users);

                                                Intent intent = new Intent(OrderConfirmationActivity.this, MainActivity.class);
                                                startActivity(intent);

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
        });
    }

    private Task<String> sendEmail() {
        Map<String, String> data = new HashMap<>();
        data.put("email", "rami.abushaqra79@gmail.com");

        return functions
                .getHttpsCallable("sendEmail")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        return (String) task.getResult().getData();
                    }
                });
    }
}