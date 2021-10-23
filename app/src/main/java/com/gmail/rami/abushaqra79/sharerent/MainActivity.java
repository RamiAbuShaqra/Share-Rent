package com.gmail.rami.abushaqra79.sharerent;

import androidx.annotation.NonNull;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // TODO check the read and write rules in realtime database
    // TODO reformat and clean the code for the whole project (comments, strings, styles)
    // TODO check if online payment can be done in OrderSummaryActivity
    // TODO send notifications to users in OrderSummaryActivity

    // TODO BUG ---> selected city not saved in MainActivity when rotating screen
    // TODO BUG ---> selected items (categories) not saved in ChooseItemsActivity when rotating screen
    // TODO BUG ---> if a NEW user logged out then logged in (without killing the app), all user personal info will be reset
    // TODO BUG ---> logged in user should be automatically logged out if the app is in background
    // TODO BUG ---> if the user updated the photo of his item, it is not updated directly (Async)
    // TODO BUG ---> the string-array doesn't work for the baby gear items spinner

    public static final int RESET_THE_CART = -100;

    @SuppressLint("StaticFieldLeak")
    public static TextView cartTV;

    private Spinner cities;
    private Calendar myCalendar;
    private EditText startRentalDate;
    private EditText endRentalDate;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        int numberOfItems = PreferenceActivity.CartPreferenceFragment.updateCart(0);

        RelativeLayout badgeLayout = (RelativeLayout) menu.findItem(R.id.shopping_cart).getActionView();
        cartTV = badgeLayout.findViewById(R.id.number_of_items_in_cart);
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

        Spinner countries = findViewById(R.id.countries_list);
        cities = findViewById(R.id.cities_list);

        ArrayAdapter<CharSequence> countriesAdapter = ArrayAdapter.createFromResource(this,
                R.array.countries_list, android.R.layout.simple_spinner_item);
        countriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countries.setAdapter(countriesAdapter);

        countries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCountry = countries.getSelectedItem().toString();

                switch (selectedCountry) {
                    case "Bahrain" :
                        settingSpinnerAdapter(R.array.bahrain);
                        break;

                    case "Egypt" :
                        settingSpinnerAdapter(R.array.egypt);
                        break;

                    case "Jordan" :
                        settingSpinnerAdapter(R.array.jordan);
                        break;

                    case "Kuwait" :
                        settingSpinnerAdapter(R.array.kuwait);
                        break;

                    case "Oman" :
                        settingSpinnerAdapter(R.array.oman);
                        break;

                    case "Qatar" :
                        settingSpinnerAdapter(R.array.qatar);
                        break;

                    case "Saudi Arabia" :
                        settingSpinnerAdapter(R.array.saudi_arabia);
                        break;

                    case "United Arab Emirates" :
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
                String country = countries.getSelectedItem().toString();
                String city = cities.getSelectedItem().toString();
                String startDate = startRentalDate.getText().toString();
                String endDate = endRentalDate.getText().toString();

                if (country.equals("Select Country..")) {
                    ((TextView)countries.getSelectedView()).setError("Please select a country");
                    return;
                }

                if (city.equals("Select City..")) {
                    ((TextView)cities.getSelectedView()).setError("Please select a city");
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

    private void settingSpinnerAdapter(int arrayResource) {
        ArrayAdapter<CharSequence> citiesAdapter = ArrayAdapter.createFromResource(
                MainActivity.this, arrayResource, android.R.layout.simple_spinner_item);
        citiesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cities.setAdapter(citiesAdapter);
        cities.setEnabled(true);
        cities.setClickable(true);
    }

    private String updateLabel() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);

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
