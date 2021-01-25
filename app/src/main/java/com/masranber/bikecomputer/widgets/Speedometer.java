package com.masranber.bikecomputer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.masranber.bikecomputer.R;

public class Speedometer extends RelativeLayout {

    public enum Units {
        IMPERIAL, METRIC;

        public static Units match(String unitsString) {
            for(Units units : Units.values()) {
                if(units.toString().equals(unitsString)) {
                    return units;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    TextView speedWholeText;
    TextView speedFractionText;
    TextView speedUnitsText;

    private float speed;
    private Units units;

    public Speedometer(Context context) {
        super(context);
        init(context, null);
    }

    public Speedometer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.speedometer_widget, this);

        speedWholeText = findViewById(R.id.speed_whole);
        speedFractionText = findViewById(R.id.speed_fraction);
        speedUnitsText = findViewById(R.id.speed_units);
    }

    public void update(float speed, Units units) {
        setSpeed(speed);
        setUnits(units);
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        if(speed >= 100.0f) {
            throw new IllegalArgumentException("Invalid speed >= 100");
        }
        if(speed < 0.0f) {
            throw new IllegalArgumentException("Invalid speed < 0");
        }
        this.speed = (this.units == Units.IMPERIAL) ? speed : convertMiToKm(speed);
        String speedString = String.valueOf(this.speed);
        String[] speedParts = speedString.split("\\.");
        speedWholeText.setText(speedParts[0]);
        speedFractionText.setText(speedParts[1].substring(0, 1)); // TODO Should allow specifying decimal places?
    }

    public Units getUnits() {
        return units;
    }

    public void setUnits(Units units) {
        this.units = units;
        speedUnitsText.setText(units == Units.IMPERIAL ? "mph" : "kph");
        setSpeed(this.speed);
    }

    // Works for any function of distance (so also speed)
    public static float convertMiToKm(float miles) {
        return (float) (miles*1.609344);
    }
}
