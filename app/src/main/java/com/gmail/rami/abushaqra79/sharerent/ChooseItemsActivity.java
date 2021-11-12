package com.gmail.rami.abushaqra79.sharerent;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ChooseItemsActivity extends MainActivity {

    private Calendar myCalendar;
    private EditText startRentalDate;
    private EditText endRentalDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_items);

        // Get Intent with extras as a bundle
        Bundle bundle = getIntent().getExtras();

        String travelDestination = bundle.getString("Destination");
        String startDate = bundle.getString("Start Date");
        String endDate = bundle.getString("End Date");

        TextView destination = findViewById(R.id.destination);
        startRentalDate = findViewById(R.id.start_date);
        endRentalDate = findViewById(R.id.end_date);

        destination.setText(travelDestination);
        startRentalDate.setText(startDate);
        endRentalDate.setText(endDate);

        myCalendar = Calendar.getInstance();

        DatePickerDialog.OnDateSetListener firstDate = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            startRentalDate.setText(updateLabel());
        };

        DatePickerDialog.OnDateSetListener secondDate = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            endRentalDate.setText(updateLabel());
        };

        // When the date field is clicked, show the date picker
        startRentalDate.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(ChooseItemsActivity.this,
                    firstDate, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH));

            // Set today's date as minimum date and all the past dates are disabled
            datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePicker.show();
        });

        // When the date field is clicked, show the date picker
        endRentalDate.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(ChooseItemsActivity.this,
                    secondDate, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH));

            if (getStartDate() == 0) {
                datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            } else {
                // Set the minimum date to be the day after the start date
                datePicker.getDatePicker().setMinDate(getStartDate());
            }

            datePicker.show();
        });

        LinearLayout strollerLayout = findViewById(R.id.stroller_layout);
        strollerLayout.setOnClickListener(v -> strollerLayout.setSelected(!strollerLayout.isSelected()));

        LinearLayout bedLayout = findViewById(R.id.bed_layout);
        bedLayout.setOnClickListener(v -> bedLayout.setSelected(!bedLayout.isSelected()));

        LinearLayout carSeatLayout = findViewById(R.id.car_seat_layout);
        carSeatLayout.setOnClickListener(v -> carSeatLayout.setSelected(!carSeatLayout.isSelected()));

        LinearLayout highChairLayout = findViewById(R.id.high_chair_layout);
        highChairLayout.setOnClickListener(v -> highChairLayout.setSelected(!highChairLayout.isSelected()));

        LinearLayout bathTubLayout = findViewById(R.id.bath_tub_layout);
        bathTubLayout.setOnClickListener(v -> bathTubLayout.setSelected(!bathTubLayout.isSelected()));

        LinearLayout bouncerLayout = findViewById(R.id.bouncer_layout);
        bouncerLayout.setOnClickListener(v -> bouncerLayout.setSelected(!bouncerLayout.isSelected()));

        LinearLayout sterilizerLayout = findViewById(R.id.sterilizer_layout);
        sterilizerLayout.setOnClickListener(v -> sterilizerLayout.setSelected(!sterilizerLayout.isSelected()));

        TextView continueBtn = findViewById(R.id.continue_btn);
        continueBtn.setOnClickListener(v -> {
            // If no item selected, display a toast message and return
            if (!strollerLayout.isSelected() && !bedLayout.isSelected() &&
                    !carSeatLayout.isSelected() && !highChairLayout.isSelected() &&
                    !bathTubLayout.isSelected() && !bouncerLayout.isSelected() &&
                    !sterilizerLayout.isSelected()) {
                Toast.makeText(ChooseItemsActivity.this,
                        "Please select items to continue", Toast.LENGTH_LONG).show();
                return;
            }

            // ArrayList to store the selected items
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

            String firstDate1 = startRentalDate.getText().toString();
            String secondDate1 = endRentalDate.getText().toString();
            int totalDays = numberOfDays(firstDate1, secondDate1);

            Intent intent = new Intent(ChooseItemsActivity.this, ReviewItemsOptions.class);
            intent.putExtra("Destination", travelDestination);
            intent.putExtra("Start Date", firstDate1);
            intent.putExtra("End Date", secondDate1);
            intent.putExtra("Total Days", totalDays);
            intent.putStringArrayListExtra("Selected Items", items);
            startActivity(intent);
        });
    }

    /**
     * Get the selected date as String.
     *
     * @return a selected date in a format of (dd/MM/yyyy)
     */
    private String updateLabel() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);

        return simpleDateFormat.format(myCalendar.getTime());
    }

    /**
     * Get the selected start date so the click listener of the end date can set the minimum date
     * as the day after of that date.
     *
     * @return a selected start date as a long time in milliseconds, or 0 if start date not selected
     */
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

    /**
     * Calculate the number of days between the selected start and selected end dates.
     *
     * @param firstDate  is the selected start date.
     * @param secondDate is the selected end date.
     *
     * @return The total number of booking days (the difference between start date and end date
     * in days).
     */
    private int numberOfDays(String firstDate, String secondDate) {
        int totalDays = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);

        try {
            Date date_1 = simpleDateFormat.parse(firstDate);
            Date date_2 = simpleDateFormat.parse(secondDate);

            if (date_1 != null && date_2 != null) {
                long diff = date_2.getTime() - date_1.getTime();
                totalDays = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //PreferenceActivity.CartPreferenceFragment.setNumberOfRentDays(totalDays);
        return totalDays;
    }
}