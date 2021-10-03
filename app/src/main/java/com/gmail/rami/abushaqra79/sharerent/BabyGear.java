package com.gmail.rami.abushaqra79.sharerent;

public class BabyGear {
    private final String babyGearType;
    private final String babyGearDescription;
    private final String rentPrice;
    private final String imageUrl;

    public BabyGear(String babyGearType,
                    String babyGearDescription,
                    String rentPrice,
                    String imageUrl) {
        this.babyGearType = babyGearType;
        this.babyGearDescription = babyGearDescription;
        this.rentPrice = rentPrice;
        this.imageUrl = imageUrl;
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

    public String getImageUrl() {
        return imageUrl;
    }
}
