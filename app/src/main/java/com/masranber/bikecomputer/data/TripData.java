package com.masranber.bikecomputer.data;

public class TripData {

    private long elapsedMillis;
    private float distance;
    private int rpm;

    public TripData(long elapsedMillis, float distance, int rpm) {
        this.elapsedMillis = elapsedMillis;
        this.distance = distance;
        this.rpm = rpm;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public float getDistance() {
        return distance;
    }

    public int getRpm() {
        return rpm;
    }
}
