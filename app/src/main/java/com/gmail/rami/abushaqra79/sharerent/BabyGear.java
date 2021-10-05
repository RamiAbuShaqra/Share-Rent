package com.gmail.rami.abushaqra79.sharerent;

public class BabyGear {

    private String babyGearType;
    private String babyGearDescription;
    private String rentPrice;
    private String imageUrl;
    private String storagePath;

    // Default constructor required for calls to DataSnapshot.getValue(BabyGear.class)
    public BabyGear() {
    }

    public BabyGear(String babyGearType,
                    String babyGearDescription,
                    String rentPrice,
                    String imageUrl,
                    String storagePath) {
        this.babyGearType = babyGearType;
        this.babyGearDescription = babyGearDescription;
        this.rentPrice = rentPrice;
        this.imageUrl = imageUrl;
        this.storagePath = storagePath;
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

    public String getStoragePath() {
        return storagePath;
    }
}
