package com.gmail.rami.abushaqra79.sharerent;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ChooseItemsActivity extends MainActivity {

    private static final String TAG = ChooseItemsActivity.class.getSimpleName();
    private Calendar myCalendar;
    private EditText startRentalDate;
    private EditText endRentalDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_items);

        Bundle bundle = getIntent().getExtras();

        String travelDestination = bundle.getString("Destination");
        String startDate = bundle.getString("Start Date");
        String endDate = bundle.getString("End Date");

        AutoCompleteTextView location = findViewById(R.id.destination);
        startRentalDate = findViewById(R.id.start_date);
        endRentalDate = findViewById(R.id.end_date);

        location.setText(travelDestination);
        startRentalDate.setText(startDate);
        endRentalDate.setText(endDate);

        myCalendar = Calendar.getInstance();

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
                Log.e(TAG, "Error! Not able to get data");
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, locations);
        location.setAdapter(adapter);
        location.setThreshold(1);

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
                DatePickerDialog datePicker = new DatePickerDialog(ChooseItemsActivity.this,
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
                DatePickerDialog datePicker = new DatePickerDialog(ChooseItemsActivity.this,
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

        LinearLayout strollerLayout = findViewById(R.id.stroller_layout);
        strollerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strollerLayout.setSelected(!strollerLayout.isSelected());
            }
        });

        LinearLayout bedLayout = findViewById(R.id.bed_layout);
        bedLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bedLayout.setSelected(!bedLayout.isSelected());
            }
        });

        LinearLayout carSeatLayout = findViewById(R.id.car_seat_layout);
        carSeatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carSeatLayout.setSelected(!carSeatLayout.isSelected());
            }
        });

        LinearLayout highChairLayout = findViewById(R.id.high_chair_layout);
        highChairLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                highChairLayout.setSelected(!highChairLayout.isSelected());
            }
        });

        LinearLayout bathTubLayout = findViewById(R.id.bath_tub_layout);
        bathTubLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bathTubLayout.setSelected(!bathTubLayout.isSelected());
            }
        });

        LinearLayout bouncerLayout = findViewById(R.id.bouncer_layout);
        bouncerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bouncerLayout.setSelected(!bouncerLayout.isSelected());
            }
        });

        LinearLayout sterilizerLayout = findViewById(R.id.sterilizer_layout);
        sterilizerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sterilizerLayout.setSelected(!sterilizerLayout.isSelected());
            }
        });

        TextView continueBtn = findViewById(R.id.continue_btn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!strollerLayout.isSelected() && !bedLayout.isSelected() &&
                        !carSeatLayout.isSelected() && !highChairLayout.isSelected() &&
                        !bathTubLayout.isSelected() && !bouncerLayout.isSelected() &&
                        !sterilizerLayout.isSelected()) {
                    Toast.makeText(ChooseItemsActivity.this,
                            "Please select items to continue", Toast.LENGTH_LONG).show();
                    return;
                }

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

                ArrayList<String> items = new ArrayList<>();

                if (strollerLayout.isSelected()) {
                    items.add("Stroller");
                }

                if (bedLayout.isSelected()) {
                    items.add("Bed");
                }

                if (carSeatLayout.isSelected()) {
                    items.add("Car Seat");
                }

                if (highChairLayout.isSelected()) {
                    items.add("High Chair");
                }

                if (bathTubLayout.isSelected()) {
                    items.add("Bath Tub");
                }

                if (bouncerLayout.isSelected()) {
                    items.add("Bouncer");
                }

                if (sterilizerLayout.isSelected()) {
                    items.add("Sterilizer");
                }

                Intent intent = new Intent(ChooseItemsActivity.this, ReviewItemsOptions.class);
                intent.putExtra("Destination", destination);
                intent.putExtra("Start Date", startDate);
                intent.putExtra("End Date", endDate);
                intent.putStringArrayListExtra("Selected Items", items);
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
}