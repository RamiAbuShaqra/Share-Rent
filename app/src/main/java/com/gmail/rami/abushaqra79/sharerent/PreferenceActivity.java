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

        public static void addItemToPreference(ArrayList<BabyGear> list) {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(MyApplication.getAppContext());
            SharedPreferences.Editor editor = prefs.edit();

            Gson gson = new Gson();
            String json = gson.toJson(list);

            editor.putString("summary_items", json);
            editor.apply();
        }

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
    }
}