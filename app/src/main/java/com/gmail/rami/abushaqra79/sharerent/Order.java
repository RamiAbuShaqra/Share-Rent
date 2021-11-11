package com.gmail.rami.abushaqra79.sharerent;

import java.util.ArrayList;

public class Order {
    private final String supplierEmail;
    private final String supplierName;
    private final String phoneNumber;
    private final String photoUrl;
    private final String supplierToken;
    private final ArrayList<BabyGear> listItems;

    public Order(String supplierEmail, String supplierName, String phoneNumber, String photoUrl,
                 String supplierToken, ArrayList<BabyGear> listItems) {
        this.supplierEmail = supplierEmail;
        this.supplierName = supplierName;
        this.phoneNumber = phoneNumber;
        this.photoUrl = photoUrl;
        this.supplierToken = supplierToken;
        this.listItems = listItems;
    }

    public String getSupplierEmail() {
        return supplierEmail;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getSupplierToken() {
        return supplierToken;
    }

    public ArrayList<BabyGear> getListItems() {
        return listItems;
    }
}