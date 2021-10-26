package com.gmail.rami.abushaqra79.sharerent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.gmail.rami.abushaqra79.MyApplication;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
    }

    public static class CartPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        /**
         * Update the number of items in the shopping cart.
         *
         * @param items number of items to be added or removed from cart.
         * @return the current number of items in the cart.
         */
        public static int updateCart(int items) {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(MyApplication.getAppContext());
            int currentItems = prefs.getInt("cart_items", 0);

            if (items == -100){
                currentItems = 0;
            } else {
                currentItems += items;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("cart_items", currentItems);
            editor.apply();

            return prefs.getInt("cart_items", 0);
        }

        /**
         * Save the number of days for renting items in the Shared Preferences.
         *
         * @param number the number of days to be saved.
         */
        public static void setNumberOfRentDays(int number) {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(MyApplication.getAppContext());

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("number_of_days", number);
            editor.apply();
        }

        /**
         * Retrieve the number of days for renting items from the Shared Preferences.
         *
         * @return the number of days saved in Shared Preferences.
         */
        public static int getNumberOfRentDays() {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(MyApplication.getAppContext());
            return prefs.getInt("number_of_days", 0);
        }

        /**
         * Save a list of items that were added to cart.
         *
         * @param list the list of items that were added to cart.
         */
        public static void addItemToPreference(ArrayList<BabyGear> list) {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(MyApplication.getAppContext());
            SharedPreferences.Editor editor = prefs.edit();

            Gson gson = new Gson();
            String json = gson.toJson(list);

            editor.putString("summary_items", json);
            editor.apply();
        }

        /**
         * Get the list of items that were added to cart.
         *
         * @return list of baby gear items in the cart.
         */
        public static ArrayList<BabyGear> getSummaryOfItems() {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(MyApplication.getAppContext());
            String json = prefs.getString("summary_items", "");

            Gson gson = new Gson();
            Type type = new TypeToken<List<BabyGear>>() {}.getType();

            ArrayList<BabyGear> items = gson.fromJson(json, type);

            if (items == null){
                return new ArrayList<>();
            } else return items;
        }

        /**
         * Save a list of users (item providers) for the items added in the cart.
         *
         * @param list the list of users to be saved.
         */
        public static void addItemProviderToPreference(ArrayList<User> list) {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(MyApplication.getAppContext());
            SharedPreferences.Editor editor = prefs.edit();

            Gson gson = new Gson();
            String json = gson.toJson(list);

            editor.putString("items_providers", json);
            editor.apply();
        }

        /**
         * Get the list of users (item providers) for the items in the cart.
         *
         * @return the list of users (item providers) for items in the cart.
         */
        public static ArrayList<User> getSummaryOfItemsProviders() {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(MyApplication.getAppContext());
            String json = prefs.getString("items_providers", "");

            Gson gson = new Gson();
            Type type = new TypeToken<List<User>>() {}.getType();

            ArrayList<User> users = gson.fromJson(json, type);

            if (users == null){
                return new ArrayList<>();
            } else return users;
        }
    }
}