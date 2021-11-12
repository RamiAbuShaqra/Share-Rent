package com.gmail.rami.abushaqra79.sharerent;

public class BookingDates {

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
