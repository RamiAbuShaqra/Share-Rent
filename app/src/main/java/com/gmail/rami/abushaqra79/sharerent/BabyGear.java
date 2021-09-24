package com.gmail.rami.abushaqra79.sharerent;

import android.net.Uri;

public class BabyGear {
    private final String babyGearType;
    private final String babyGearDescription;
    private final String rentPrice;
    private final Uri imageUri;

    public BabyGear(String babyGearType, String babyGearDescription, String rentPrice, Uri imageUri) {
        this.babyGearType = babyGearType;
        this.babyGearDescription = babyGearDescription;
        this.rentPrice = rentPrice;
        this.imageUri = imageUri;
    }

    public String getBabyGearType() {
        return babyGearType;
    }

    public String getBabyGearDescription() {
        return babyGearDescription;
    }

    public String getRentPrice() {
        return rentPrice;
    }

    public Uri getImageUri() {
        return imageUri;
    }
}
