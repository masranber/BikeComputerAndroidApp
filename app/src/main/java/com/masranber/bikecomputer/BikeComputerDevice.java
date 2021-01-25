package com.masranber.bikecomputer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.masranber.bikecomputer.data.DeviceData;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

public class BikeComputerDevice {

    private static final String TAG = "BikeComputerDevice";

    public interface ConnectionListener {
        void onConnect();
        void onDisconnect(BTError reason);
    }

    public interface DataListener {
        void onDataReceived(DeviceData deviceData);
    }

    public enum BTError {
        BT_NOT_SUPPORTED, BT_OFF, BT_NOT_PAIRED, BT_CONN_REFUSED, BT_CONN_TIMEOUT, BT_DISCONNECT;
    }

    private static BikeComputerDevice bikeComputerDevice;
    private static final String BLUETOOTH_UUID = "00001101-0000-1000-8000-00805f9b34fb"; // Universal SPP (Serial Port Profile) service UUID
    private static final String DEVICE_NAME = "HC-05";
    private static final int READ_BUFFER_SIZE_BYTES = 1024;


    private Handler mainThreadHandler; // Used to return callbacks to main thread

    private BluetoothDevice device;
    private BluetoothSocket bluetoothSocket;
    private ConnectionListener connectionListener;
    private DataListener dataListener;

    //public Observable<DeviceData> deviceData = new Observable<>();
    public final MutableLiveData<DeviceData> deviceData = new MutableLiveData<>();

    @Inject
    public BikeComputerDevice() {
        mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    public static BikeComputerDevice getDevice() {
        if(bikeComputerDevice == null) {
            bikeComputerDevice = new BikeComputerDevice();
            return bikeComputerDevice;
        }
        return bikeComputerDevice;
    }

    public void connect(final ConnectionListener listener) {
        this.connectionListener = listener;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            listener.onDisconnect(BTError.BT_NOT_SUPPORTED);
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            listener.onDisconnect(BTError.BT_OFF);
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        device = null;
        if(pairedDevices.size() == 0) {
            listener.onDisconnect(BTError.BT_NOT_PAIRED);
            return;
        } else {
            for (BluetoothDevice pairedDevice : pairedDevices) {
                if(pairedDevice.getName().equals(DEVICE_NAME)) device = pairedDevice;
                //String deviceHardwareAddress = device.getAddress(); // MAC address
            }
            if(device == null) {
                listener.onDisconnect(BTError.BT_NOT_PAIRED);
                return;
            }
        }
        // Get device UUID used to connect, this is static and should be hardcoded
        /*ParcelUuid[] uuids = device.getUuids();
        for(ParcelUuid uuid : uuids) {
            Log.i(TAG, uuid.toString());
        }*/
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(BLUETOOTH_UUID));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    try {
                        // Connect to the bluetooth device through the socket. This call blocks
                        // until it succeeds or throws an exception.
                        bluetoothSocket.connect();
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // Connection success, begin reading data
                                listener.onConnect();
                                readAsync();
                            }
                        });
                    } catch (final IOException connectException) {
                        // Unable to connect...
                        try {
                            bluetoothSocket.close();
                            mainThreadHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onDisconnect(BTError.BT_CONN_TIMEOUT);
                                }
                            });
                        } catch (IOException closeException) {
                            mainThreadHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onDisconnect(BTError.BT_CONN_REFUSED);
                                }
                            });
                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            // Not sure this catch block even functions since the above code is running in a separate thread...
            listener.onDisconnect(BTError.BT_CONN_REFUSED);
        }
    }

    private void readAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                try {
                    inputStream = bluetoothSocket.getInputStream();
                    while(bluetoothSocket.isConnected()) {
                        long start = SystemClock.elapsedRealtime();
                        final DeviceData data = DeviceData.fromBytes(readPacket(inputStream)); // Read incoming data packet from bluetooth and deserialize it into POJO
                        deviceData.postValue(data);
                        long elapsed = SystemClock.elapsedRealtime() - start;
                        Log.i(TAG, data+" in "+elapsed+" ms");
                    }
                    // Loop exits due to loss of connection
                    // Usually an IOException will already be thrown by something in the while loop before isConnected returns false
                    // This is just an additional safety net
                    throw new IOException();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(connectionListener != null) {
                        mainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                connectionListener.onDisconnect(BTError.BT_DISCONNECT);
                            }
                        });
                    }
                    try {
                        if(inputStream != null) inputStream.close();
                        bluetoothSocket.close();
                    } catch (IOException ex) {
                        // Usually this exception gets thrown due to bluetooth socket already being closed
                        // Not a cause for concern
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }

    // Warning this is BLOCKING
    private byte[] readPacket(InputStream inputStream) throws IOException {
        byte[] packetBuffer = new byte[DeviceData.DATA_BYTES]; // Our data packets are 8 bytes in length
        while ((char) inputStream.read() != DeviceData.START_BYTE) {
            // Wait until until we receive the char that marks the beginning of a packet
        }
        // Fill our data buffer with the incoming serial data
        for (int i = 0; i < packetBuffer.length; i++) {
            packetBuffer[i] = (byte) inputStream.read();
        }
        return packetBuffer;
    }

    public void attachConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }

    public void attachDataListener(DataListener listener) {
        this.dataListener = listener;
    }
}
