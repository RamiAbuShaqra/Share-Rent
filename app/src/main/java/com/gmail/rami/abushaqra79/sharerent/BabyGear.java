package com.gmail.rami.abushaqra79.sharerent;

public class BabyGear {
    private final String babyGearType;
    private final String babyGearDescription;
    private final String rentPrice;

    public BabyGear(String babyGearType, String babyGearDescription, String rentPrice) {
        this.babyGearType = babyGearType;
        this.babyGearDescription = babyGearDescription;
        this.rentPrice = rentPrice;
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
}
