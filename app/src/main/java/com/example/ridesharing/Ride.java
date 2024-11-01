package com.example.ridesharing;

public class Ride {
    public String userId;
    public String phone;
    public String vehicleType;
    public String seats;
    public String time;
    public String date;
    public String vehicleNumber;
    public String startingLocation; // New field
    public String endingLocation; // New field

    // Default constructor required for Firebase
    public Ride() {
    }

    public Ride(String userId, String phone, String vehicleType, String seats, String time, String date, String vehicleNumber, String startingLocation, String endingLocation) {
        this.userId = userId;
        this.phone = phone;
        this.vehicleType = vehicleType;
        this.seats = seats;
        this.time = time;
        this.date = date;
        this.vehicleNumber = vehicleNumber;
        this.startingLocation = startingLocation; // New field
        this.endingLocation = endingLocation; // New field
    }
}
