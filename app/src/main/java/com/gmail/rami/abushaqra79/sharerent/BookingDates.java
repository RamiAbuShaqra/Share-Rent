package com.gmail.rami.abushaqra79.sharerent;

/**
 * Model class for the booking dates and total days duration.
 */
public class BookingDates {

    // Fields
    private final String startDate;
    private final String endDate;
    private final int totalDays;

    public BookingDates(String startDate, String endDate, int totalDays) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalDays = totalDays;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public int getTotalDays() {
        return totalDays;
    }
}
