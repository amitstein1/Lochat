package com.example.stein.client_1;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Created by stein on 12/01/2018.
 */

public class Location {
    private double latitude;
    private double longitude;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return String.format(String.valueOf(this.latitude)+"|"+String.valueOf(this.longitude));
    }
}
