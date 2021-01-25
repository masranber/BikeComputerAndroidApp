package com.masranber.bikecomputer.ui.main;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import com.masranber.bikecomputer.ControlButton;
import com.masranber.bikecomputer.data.DrivetrainData;
import com.masranber.bikecomputer.Event;
import com.masranber.bikecomputer.R;
import com.masranber.bikecomputer.SettingsFragment;
import com.masranber.bikecomputer.data.SpeedometerData;
import com.masranber.bikecomputer.data.TripData;
import com.masranber.bikecomputer.widgets.GearIndicator;
import com.masranber.bikecomputer.widgets.Speedometer;
import com.masranber.bikecomputer.widgets.TripDisplay;

import dagger.hilt.android.AndroidEntryPoint;

import static android.app.Activity.RESULT_OK;

@AndroidEntryPoint
public class BikeComputerFragment extends Fragment {

    private BikeComputerViewModel viewModel;

    private static final int REQUEST_ENABLE_BT = 919;

    private GearIndicator gearIndicator;
    private Speedometer speedometer;
    private TripDisplay tripDisplay;
    private ControlButton controlButton;
    private ImageButton settingsButton;
    private ToggleButton lightsButton;

    public static BikeComputerFragment newInstance() {
        return new BikeComputerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bike_computer_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(BikeComputerViewModel.class);
        // TODO: Use the ViewModel

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(sharedPreferences.getBoolean("dark_mode", false)) {
            getView().setBackgroundResource(android.R.color.black);
            getActivity().getWindow().setNavigationBarColor(getResources().getColor(android.R.color.black));
            getActivity().getWindow().setStatusBarColor(getResources().getColor(android.R.color.black));
        } else {
            getActivity().getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        speedometer = getView().findViewById(R.id.speedometer);
        gearIndicator = getView().findViewById(R.id.gear_indicator);
        controlButton = getView().findViewById(R.id.trip_control_button);
        tripDisplay = getView().findViewById(R.id.trip_display);
        settingsButton = getView().findViewById(R.id.settings_button);
        lightsButton = getView().findViewById(R.id.lights_button);


        // Initialize display widgets
        viewModel.handleUnitsChanged(sharedPreferences.getBoolean("units",true));
        tripDisplay.reset();

        observeState();
        observeData();

        controlButton.setState(viewModel.controlState.getValue());
        controlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.handleControlStateChange();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, new SettingsFragment());
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        lightsButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean toggled) {
                if(toggled) {
                    compoundButton.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.colorAccent));
                    toggleFlashlight(true);
                } else {
                    compoundButton.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.colorPrimary));
                    toggleFlashlight(false);
                }
            }
        });

        // If auto connect is enabled and device is currently disconnected, alert view model to begin connection
        if(sharedPreferences.getBoolean("auto_connect", false) && (controlButton.getState() == ControlButton.State.DISCONNECTED)) {
            controlButton.performClick();
        }
    }

    private void observeState() {
        viewModel.controlState.observe(getViewLifecycleOwner(), new Observer<ControlButton.State>() {
            @Override
            public void onChanged(ControlButton.State state) {
                controlButton.setState(state);
            }
        });
        viewModel.actionRequiredEvent.observe(getViewLifecycleOwner(), new Observer<Event<BikeComputerViewModel.Action>>() {
            @Override
            public void onChanged(Event<BikeComputerViewModel.Action> actionEvent) {
                if(!actionEvent.hasBeenHandled()) {
                    switch(actionEvent.getContent()) {
                        case REQUEST_BLUETOOTH:
                            requestEnableBluetooth();
                            break;
                        case RESET_TRIP:
                            tripDisplay.reset();
                            break;
                    }
                }
            }
        });
    }

    private void observeData() {
        viewModel.currentTripData.observe(getViewLifecycleOwner(), new Observer<TripData>() {
            @Override
            public void onChanged(TripData tripData) {
                tripDisplay.update(tripData.getElapsedMillis(), tripData.getDistance(), tripData.getRpm());
            }
        });
        viewModel.currentSpeedometerData.observe(getViewLifecycleOwner(), new Observer<SpeedometerData>() {
            @Override
            public void onChanged(SpeedometerData speedometerData) {
                speedometer.setSpeed(speedometerData.getSpeed());
            }
        });
        viewModel.currentUnits.observe(getViewLifecycleOwner(), new Observer<BikeComputerViewModel.Units>() {
            @Override
            public void onChanged(BikeComputerViewModel.Units units) {
                Log.i("COMPUTER", "Changing display units to "+units.toString());
                tripDisplay.setUnits(TripDisplay.Units.match(units.toString()));
                speedometer.setUnits(Speedometer.Units.match(units.toString()));
            }
        });
        viewModel.currentDrivetrainData.observe(getViewLifecycleOwner(), new Observer<DrivetrainData>() {
            @Override
            public void onChanged(DrivetrainData drivetrainData) {
                gearIndicator.setCurrentGear(drivetrainData.getFrontGear(), drivetrainData.getRearGear());
            }
        });
    }

    public void requestEnableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    public void toggleFlashlight(boolean enabled) {
        CameraManager camManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        String cameraId = null;
        try {
            cameraId = camManager.getCameraIdList()[0];
            camManager.setTorchMode(cameraId, enabled);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            // Bluetooth is good to go, alert view model so it can begin connection
            viewModel.handleControlStateChange();
        }
    }


}