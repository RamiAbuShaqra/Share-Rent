package com.gmail.rami.abushaqra79.sharerent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // TODO check the read and write rules in realtime database

    public static final int RESET_THE_CART = -100;
    private Calendar myCalendar;
    private EditText startRentalDate;
    private EditText endRentalDate;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        int numberOfItems = PreferenceActivity.CartPreferenceFragment.updateCart(0);

        RelativeLayout badgeLayout = (RelativeLayout) menu.findItem(R.id.shopping_cart).getActionView();
        TextView cartTV = badgeLayout.findViewById(R.id.number_of_items_in_cart);
        cartTV.setText(String.valueOf(numberOfItems));

        badgeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent orderSummary = new Intent(MainActivity.this, OrderSummaryActivity.class);
                startActivity(orderSummary);
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.login_menu_item) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get shared preferences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        // first time run?
        if (pref.getBoolean("firstTimeRun", true)) {
            // reset the shared preferences by passing -100 and make the cart items = 0
            PreferenceActivity.CartPreferenceFragment.updateCart(RESET_THE_CART);
            // get the preferences editor
            SharedPreferences.Editor editor = pref.edit();
            // avoid for next run
            editor.putBoolean("firstTimeRun", false);
            editor.apply();
        }

        ArrayList<String> locations = new ArrayList<>();

        ReadAndWriteDatabase rwd = new ReadAndWriteDatabase(this);
        rwd.fetchData(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String value = child.child("user-info").child("location").getValue().toString();
                        locations.add(value);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "Error! Not able to get data");
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, locations);
        AutoCompleteTextView location = findViewById(R.id.locations);
        location.setAdapter(adapter);
        location.setThreshold(1);

        myCalendar = Calendar.getInstance();

        startRentalDate = findViewById(R.id.start_date);
        endRentalDate = findViewById(R.id.end_date);

        location.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                location.setError(null);
            }
        });

        startRentalDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                startRentalDate.setError(null);
            }
        });

        endRentalDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                endRentalDate.setError(null);
            }
        });

        DatePickerDialog.OnDateSetListener firstDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                startRentalDate.setText(updateLabel());
            }
        };

        DatePickerDialog.OnDateSetListener secondDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                endRentalDate.setText(updateLabel());
            }
        };

        startRentalDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker = new DatePickerDialog(MainActivity.this,
                        firstDate, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));

                // Set today's date as minimum date and all the past dates are disabled
                datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePicker.show();
            }
        });

        endRentalDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePicker = new DatePickerDialog(MainActivity.this,
                        secondDate, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));

                if (getStartDate() == 0) {
                    datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                } else {
                    datePicker.getDatePicker().setMinDate(getStartDate());
                }

                datePicker.show();
            }
        });

        TextView continueBtn = findViewById(R.id.continue_btn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String destination = location.getText().toString();
                String startDate = startRentalDate.getText().toString();
                String endDate = endRentalDate.getText().toString();

                if (destination.equals("")) {
                    location.setError("Please enter a destination");
                    return;
                }

                if (startDate.equals("")) {
                    startRentalDate.setError("Please select start date");
                    return;
                }

                if (endDate.equals("")) {
                    endRentalDate.setError("please select end date");
                    return;
                }

                Intent intent = new Intent(MainActivity.this, ChooseItemsActivity.class);
                intent.putExtra("Destination", destination);
                intent.putExtra("Start Date", startDate);
                intent.putExtra("End Date", endDate);
                startActivity(intent);
            }
        });

        TextView signUpPage = findViewById(R.id.sign_up_page);
        signUpPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private String updateLabel() {
        String dateFormat = "dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.ROOT);

        return simpleDateFormat.format(myCalendar.getTime());
    }

    private long getStartDate() {
        final long EXTRA_DAY = 86400000; // 86,400,000 millisecond in one day.
        long time = 0;
        String startDate = startRentalDate.getText().toString();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);

        try {
            Date date = simpleDateFormat.parse(startDate);
            if (date != null) {
                time = date.getTime() + EXTRA_DAY;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return time;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        invalidateOptionsMenu();
    }
}
