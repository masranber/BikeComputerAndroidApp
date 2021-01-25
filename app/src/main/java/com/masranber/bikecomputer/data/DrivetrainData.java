package com.masranber.bikecomputer.data;

public class DrivetrainData {

    private int frontGear;
    private int rearGear;

    public DrivetrainData(int frontGear, int rearGear) {
        this.frontGear = frontGear;
        this.rearGear = rearGear;
    }

    public int getFrontGear() {
        return frontGear;
    }

    public int getRearGear() {
        return rearGear;
    }

    @Override
    public String toString() {
        return String.format("DrivetrainData [%d, %d]", frontGear, rearGear);
    }
}
