package com.masranber.bikecomputer.widgets;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.masranber.bikecomputer.R;

import java.util.concurrent.TimeUnit;

public class TripDisplay extends LinearLayout {

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

    private TextView timeText;
    private TextView distText;
    private TextView rpmText;
    private TextView avgSpdText;

    private Units units;

    private long elapsedMillis;
    private float totalDistance;
    private int currentRPM;

    public TripDisplay(Context context) {
        super(context);
        init(context, null);
    }

    public TripDisplay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.trip_display_widget, this);

        timeText = findViewById(R.id.trip_time_text);
        distText = findViewById(R.id.trip_distance_text);
        rpmText = findViewById(R.id.trip_rpm_text);
        avgSpdText = findViewById(R.id.trip_avgspeed_text);

        this.units = Units.IMPERIAL;
        //timeText.setText("00:00:00");
        //distText.setText("0.0"+" "+"units");
    }

    public void update(long elapsedMillis, float distance, int rpm) {
        this.elapsedMillis = elapsedMillis;
        this.totalDistance = (this.units == Units.IMPERIAL) ? distance : convertMiToKm(distance);
        this.currentRPM = rpm;

        timeText.setText(millisToTimestring(elapsedMillis));
        distText.setText(String.format("%.2f %s", totalDistance, this.units == Units.IMPERIAL ? "mi" : "km"));
        rpmText.setText(String.valueOf(rpm));

        float avgSpeed = totalDistance / ((float) elapsedMillis / 3600000f);
        // Don't display average speed until at least 10 seconds have passed
        if(elapsedMillis > 10000) {
            if (Float.isNaN(avgSpeed)) {
                avgSpdText.setText(String.format("%.1f %s", 0.0, this.units == Units.IMPERIAL ? "mph" : "kph"));
            } else {
                avgSpdText.setText(String.format("%.1f %s", avgSpeed, this.units == Units.IMPERIAL ? "mph" : "kph"));
            }
        } else {
            avgSpdText.setText(String.format("-- %s", this.units == Units.IMPERIAL ? "mph" : "kph"));
        }
    }

    public void reset() {
        this.elapsedMillis = 0;
        this.totalDistance = 0;
        this.currentRPM = 0;

        timeText.setText(millisToTimestring(elapsedMillis));
        distText.setText(String.format("%.2f %s", totalDistance, this.units == Units.IMPERIAL ? "mi" : "km"));
        rpmText.setText(String.valueOf(currentRPM));
        avgSpdText.setText(String.format("-- %s", this.units == Units.IMPERIAL ? "mph" : "kph"));

    }

    public void setUnits(Units units) {
        this.units = units;
        update(elapsedMillis, totalDistance, currentRPM);
    }

    public static String millisToTimestring(long elapsedMillis) {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(elapsedMillis),
                TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % TimeUnit.MINUTES.toSeconds(1));
    }

    // Works for any function of distance (so also speed)
    public static float convertMiToKm(float miles) {
        return (float) (miles*1.609344);
    }
}
