package com.gmail.rami.abushaqra79.sharerent;

public class BabyGear {
    private final String babyGearType;
    private final String babyGearDescription;
    private final int rentPrice;
    private final int photoId;

    public BabyGear(String babyGearType, String babyGearDescription, int rentPrice, int photoId) {
        this.babyGearType = babyGearType;
        this.babyGearDescription = babyGearDescription;
        this.rentPrice = rentPrice;
        this.photoId = photoId;
    }

    public String getBabyGearType() {
        return babyGearType;
    }

    public String getBabyGearDescription() {
        return babyGearDescription;
    }

    public int getRentPrice() {
        return rentPrice;
    }

    public int getPhotoId() {
        return photoId;
    }
}
