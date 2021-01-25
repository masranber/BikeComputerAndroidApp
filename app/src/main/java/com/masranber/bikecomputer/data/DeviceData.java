package com.masranber.bikecomputer.data;

import java.nio.ByteBuffer;
import java.util.IllegalFormatException;

public class DeviceData {

    //----------------------------------------------------------------------------------------------
    /*
                                            DEVICE DATA PACKET
        |    START   |                             DATA (8 bytes)                               |
        |  char '$'  |  int16 rpm  |  int32 lifetime rotations  |  int8 gear F  |  int8 gear R  |
           1 byte       2 bytes               4 bytes                1 byte          1 byte
     */

    public static final char START_BYTE = '$';
    public static final int DATA_BYTES = 8;

    // Define byte offsets for the raw data
    public static final int CURRENT_RPM_OFFSET = 0;
    public static final int TOTAL_ROTATION_OFFSET = 2;
    public static final int GEAR_F_OFFSET = 6;
    public static final int GEAR_R_OFFSET = 7;


    //----------------------------------------------------------------------------------------------


    private int currentRpm;
    private long totalRotations;
    private DrivetrainData drivetrainData;

    public DeviceData(int currentRpm, long totalRotations, DrivetrainData drivetrainData) {
        this.currentRpm = currentRpm;
        this.totalRotations = totalRotations;
        this.drivetrainData = drivetrainData;
    }

    public int getCurrentRpm() {
        return currentRpm;
    }

    public void setCurrentRpm(int currentRpm) {
        this.currentRpm = currentRpm;
    }

    public long getTotalRotations() {
        return totalRotations;
    }

    public void setTotalRotations(long totalRotations) {
        this.totalRotations = totalRotations;
    }

    public DrivetrainData getDrivetrainData() {
        return drivetrainData;
    }

    public void setDrivetrainData(DrivetrainData drivetrainData) {
        this.drivetrainData = drivetrainData;
    }

    public static DeviceData fromBytes(byte[] rawData) {
        if(rawData.length != 8) throw new IllegalArgumentException(String.format("Expected 8 bytes, received %d bytes", rawData.length));
        int currentRpm = ByteBuffer.wrap(rawData, CURRENT_RPM_OFFSET, TOTAL_ROTATION_OFFSET - CURRENT_RPM_OFFSET).getShort();
        long totalRotations = ByteBuffer.wrap(rawData, TOTAL_ROTATION_OFFSET, GEAR_F_OFFSET - TOTAL_ROTATION_OFFSET).getInt();
        int frontGear = rawData[6];
        int rearGear = rawData[7];
        return new DeviceData(currentRpm, totalRotations, new DrivetrainData(frontGear, rearGear));
    }

    @Override
    public String toString() {
        return String.format("DeviceData [%d, %d, %s]", currentRpm, totalRotations, drivetrainData.toString());
    }
}
