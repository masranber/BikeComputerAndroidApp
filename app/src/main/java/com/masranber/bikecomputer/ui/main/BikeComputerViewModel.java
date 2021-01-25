package com.masranber.bikecomputer.ui.main;

import android.os.SystemClock;
import android.util.Log;

import androidx.hilt.Assisted;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.masranber.bikecomputer.BikeComputerDevice;
import com.masranber.bikecomputer.ControlButton;
import com.masranber.bikecomputer.data.DeviceData;
import com.masranber.bikecomputer.data.DrivetrainData;
import com.masranber.bikecomputer.Event;
import com.masranber.bikecomputer.data.SpeedometerData;
import com.masranber.bikecomputer.data.TripData;

import javax.inject.Inject;

import dagger.Provides;


public class BikeComputerViewModel extends ViewModel implements BikeComputerDevice.ConnectionListener, Observer<DeviceData> {

    public enum Action {
        REQUEST_BLUETOOTH, RESET_TRIP;
    }

    public enum Units {
        IMPERIAL, METRIC;
    }

    private final BikeComputerDevice bikeComputerDevice;

    private long tripStartMillis;
    private long tripStartRotations;

    public MutableLiveData<ControlButton.State> controlState = new MutableLiveData<>(ControlButton.State.DISCONNECTED);
    public MutableLiveData<Event<Action>> actionRequiredEvent = new MutableLiveData<>();

    public MutableLiveData<TripData> currentTripData = new MutableLiveData<>(new TripData(0, 0.0f, 0));
    public MutableLiveData<SpeedometerData> currentSpeedometerData = new MutableLiveData<>(new SpeedometerData(0.0f));
    public MutableLiveData<DrivetrainData> currentDrivetrainData = new MutableLiveData<>(new DrivetrainData(0,0));
    public MutableLiveData<Units> currentUnits = new MutableLiveData<>(Units.IMPERIAL);

    private boolean tripIsStarted;

    @ViewModelInject
    public BikeComputerViewModel(@Assisted SavedStateHandle savedStateHandle, BikeComputerDevice bikeComputerDevice) {
        this.bikeComputerDevice = bikeComputerDevice;
    }

    // Implement as FSM
    public void handleControlStateChange() {
        switch(controlState.getValue()) {
            case BLUETOOTH_OFF:
                break;
            case DISCONNECTED:
                controlState.setValue(ControlButton.State.CONNECTING);
                bikeComputerDevice.connect(this);
                break;
            case CONNECTING:
                // intermediate state, no action required
                break;
            case BEGIN_TRIP:
                controlState.setValue(ControlButton.State.END_TRIP);
                startTrip();
                break;
            case END_TRIP:
                controlState.setValue(ControlButton.State.BEGIN_TRIP);
                tripIsStarted = false;
                break;
        }
    }

    public void handleUnitsChanged(boolean useMetric) {
        currentUnits.setValue(useMetric ? Units.METRIC : Units.IMPERIAL);
    }

    private void startTrip() {
        actionRequiredEvent.setValue(new Event<>(Action.RESET_TRIP));

        // Set the initial trip conditions
        tripStartMillis = SystemClock.elapsedRealtime();
        DeviceData initialData = BikeComputerDevice.getDevice().deviceData.getValue();
        tripStartRotations = initialData.getTotalRotations();
        Log.i("ViewModel", "Start trip");
        tripIsStarted = true;
    }

    public float calculateSpeedMPH(int rpm) {
        return (float) ((rpm * Math.PI * 26) / 1056.0);
    }

    public float calculateDistanceMI(long totalRotations) {
        return (float) ((totalRotations * (Math.PI * 26)) / 63360.0);
    }

    @Override
    public void onConnect() {
        controlState.setValue(ControlButton.State.BEGIN_TRIP);
        BikeComputerDevice.getDevice().deviceData.observeForever(BikeComputerViewModel.this);
    }

    @Override
    public void onDisconnect(BikeComputerDevice.BTError reason) {
        Log.i("ViewModel", "Device disconnect");
        Log.e("ViewModel", "Reason: "+reason.toString());
        controlState.setValue(ControlButton.State.DISCONNECTED);
        BikeComputerDevice.getDevice().deviceData.removeObserver(BikeComputerViewModel.this);
        if(reason == BikeComputerDevice.BTError.BT_OFF) {
            actionRequiredEvent.setValue(new Event<>(Action.REQUEST_BLUETOOTH));
        }
    }

    @Override
    public void onChanged(DeviceData deviceData) {
        int currentRpm = deviceData.getCurrentRpm();
        float currentSpeed = calculateSpeedMPH(currentRpm);
        currentSpeedometerData.setValue(new SpeedometerData(currentSpeed));
        currentDrivetrainData.setValue(deviceData.getDrivetrainData());

        if(tripIsStarted) {
            float distance = calculateDistanceMI(deviceData.getTotalRotations() - tripStartRotations);
            currentTripData.setValue(new TripData(SystemClock.elapsedRealtime() - tripStartMillis, distance, currentRpm));
        }
    }
}