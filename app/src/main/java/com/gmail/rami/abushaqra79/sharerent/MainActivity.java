package com.gmail.rami.abushaqra79.sharerent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Main activity
 */
public class MainActivity extends AppCompatActivity {

    /**
     * A code to reset the cart in the SharedPreference
     */
    public static final int RESET_THE_CART = -100;

    @SuppressLint("StaticFieldLeak")
    public static TextView cartTV; // The number of items in the cart

    /**
     * A TextView to show that there is a notification received in the action bar.
     */
    private TextView notification;

    private Spinner cities;
    private Calendar myCalendar;
    private EditText startRentalDate;
    private EditText endRentalDate;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        // Get the current number of items in the cart from Shared Preferences
        int numberOfItems = PreferenceActivity.CartPreferenceFragment.updateCart(0);

        // Shopping cart layout
        RelativeLayout badgeLayout = (RelativeLayout) menu.findItem(R.id.shopping_cart).getActionView();
        cartTV = badgeLayout.findViewById(R.id.number_of_items_in_cart);
        cartTV.setText(String.valueOf(numberOfItems));

        badgeLayout.setOnClickListener(v -> {
            Intent orderSummary = new Intent(MainActivity.this, OrderSummaryActivity.class);
            startActivity(orderSummary);
        });

        // Notification bell layout
        RelativeLayout notificationBadge = (RelativeLayout) menu.findItem(R.id.notification).getActionView();
        notification = notificationBadge.findViewById(R.id.notification_message);

        // Check if there is notification received
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null &&
                intent.getExtras().containsKey("Notification Received")) {

            int number = getIntent().getExtras().getInt("Notification Received");
            notification.setVisibility(View.VISIBLE);
            notification.setText(String.valueOf(number));

            notificationBadge.setOnClickListener(v -> notificationDialog());
        } else {
            notification.setVisibility(View.INVISIBLE);
        }

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

        // Get shared preferences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        // First time run?
        if (pref.getBoolean("firstTimeRun", true)) {
            // Reset the shared preferences by passing -100 and make the cart items = 0
            PreferenceActivity.CartPreferenceFragment.updateCart(RESET_THE_CART);
            // Get the preferences editor
            SharedPreferences.Editor editor = pref.edit();
            // Avoid for next run
            editor.putBoolean("firstTimeRun", false);
            editor.apply();
        }

        Spinner countries = findViewById(R.id.countries_list);
        cities = findViewById(R.id.cities_list);

        // Create ArrayAdapter for the countries list and attach it to the spinner
        ArrayAdapter<CharSequence> countriesAdapter = ArrayAdapter.createFromResource(this,
                R.array.countries_list, android.R.layout.simple_spinner_item);
        countriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countries.setAdapter(countriesAdapter);

        // Check the selected country and populate the cities array based on the selection
        countries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCountry = countries.getSelectedItem().toString();

                switch (selectedCountry) {
                    case "Bahrain":
                        settingSpinnerAdapter(R.array.bahrain);
                        break;

                    case "Egypt":
                        settingSpinnerAdapter(R.array.egypt);
                        break;

                    case "Jordan":
                        settingSpinnerAdapter(R.array.jordan);
                        break;

                    case "Kuwait":
                        settingSpinnerAdapter(R.array.kuwait);
                        break;

                    case "Oman":
                        settingSpinnerAdapter(R.array.oman);
                        break;

                    case "Qatar":
                        settingSpinnerAdapter(R.array.qatar);
                        break;

                    case "Saudi Arabia":
                        settingSpinnerAdapter(R.array.saudi_arabia);
                        break;

                    case "United Arab Emirates":
                        settingSpinnerAdapter(R.array.emirates);
                        break;

                    default:
                        settingSpinnerAdapter(R.array.no_selection);
                        cities.setEnabled(false);
                        cities.setClickable(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        myCalendar = Calendar.getInstance();

        startRentalDate = findViewById(R.id.start_date);
        endRentalDate = findViewById(R.id.end_date);

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
            DatePickerDialog datePicker = new DatePickerDialog(MainActivity.this,
                    firstDate, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH));

            // Set today's date as minimum date and all the past dates are disabled
            datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePicker.show();
        });

        // When the date field is clicked, show the date picker
        endRentalDate.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(MainActivity.this,
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

        TextView continueBtn = findViewById(R.id.continue_btn);
        continueBtn.setOnClickListener(v -> {
            String country = countries.getSelectedItem().toString();
            String city = cities.getSelectedItem().toString();
            String startDate = startRentalDate.getText().toString();
            String endDate = endRentalDate.getText().toString();

            if (country.equals("Select Country..")) {
                ((TextView) countries.getSelectedView()).setError("Please select a country");
                return;
            }

            if (city.equals("Select City..")) {
                ((TextView) cities.getSelectedView()).setError("Please select a city");
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

            String destination = city + ", " + country;

            Intent intent = new Intent(MainActivity.this, ChooseItemsActivity.class);
            intent.putExtra("Destination", destination);
            intent.putExtra("Start Date", startDate);
            intent.putExtra("End Date", endDate);
            startActivity(intent);
        });

        // Direct the user to sign up page
        TextView signUpPage = findViewById(R.id.sign_up_page);
        signUpPage.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Create ArrayAdapter for the cities list based on the country selection
     * and attach it to the spinner.
     *
     * @param arrayResource the string-array of cities for the selected country.
     */
    private void settingSpinnerAdapter(int arrayResource) {
        ArrayAdapter<CharSequence> citiesAdapter = ArrayAdapter.createFromResource(
                MainActivity.this, arrayResource, android.R.layout.simple_spinner_item);
        citiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cities.setAdapter(citiesAdapter);
        cities.setEnabled(true);
        cities.setClickable(true);
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
     * Creates a dialog to display the notification message received.
     */
    private void notificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Order")
                .setMessage("New Order from Share-Rent.\n" +
                        "\nPlease check your e-mail for the order details.")
                .setNegativeButton("Close", (dialog, which) -> {
                    notification.setVisibility(View.INVISIBLE);

                    if (dialog != null) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        invalidateOptionsMenu();
    }
}
