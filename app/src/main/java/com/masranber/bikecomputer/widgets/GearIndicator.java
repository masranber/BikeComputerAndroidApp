package com.masranber.bikecomputer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.masranber.bikecomputer.R;

public class GearIndicator extends LinearLayout {

    private TextView frontGearText;
    private TextView rearGearText;

    private int currentFrontGear;
    private int currentRearGear;

    public GearIndicator(Context context) {
        super(context);
        init(context, null);
    }

    public GearIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.gear_indicator_widget, this);

        frontGearText = findViewById(R.id.gear_front_text);
        rearGearText = findViewById(R.id.gear_rear_text);
    }

    private boolean isGearValid(int gear) {
        return gear >= 0 && gear < 100;
    }

    public int getCurrentFrontGear() {
        return this.currentFrontGear;
    }

    public int getCurrentRearGear() {
        return this.currentRearGear;
    }

    public void setFrontGear(int frontGear) {
        if(!isGearValid(frontGear)) throw new IllegalArgumentException();
        this.currentFrontGear = frontGear;
        frontGearText.setText(frontGear == 0 ? "-" : String.valueOf(frontGear));
    }

    public void setRearGear(int rearGear) {
        if(!isGearValid(rearGear)) throw new IllegalArgumentException();
        this.currentRearGear = rearGear;
        rearGearText.setText(rearGear == 0 ? "-" : String.valueOf(rearGear));
    }

    public void setCurrentGear(int frontGear, int rearGear) {
        if(!isGearValid(frontGear) || !isGearValid(rearGear)) throw new IllegalArgumentException();
        this.currentFrontGear = frontGear;
        this.currentRearGear = rearGear;
        frontGearText.setText(frontGear == 0 ? "-" : String.valueOf(frontGear));
        rearGearText.setText(rearGear == 0 ? "-" : String.valueOf(rearGear));
    }
}
