/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.burgr.distancewarner;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import at.burgr.distancewarner.bluetooth.BluetoothLeService;
import at.burgr.distancewarner.bluetooth.GattAttributes;
import at.burgr.distancewarner.data.Warning;
import at.burgr.distancewarner.data.WarningDao;
import at.burgr.distancewarner.gps.GpsTracker;

/**
 * This activity is shown, when we are connected to the descired BLE device. The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class ConnectedActivity extends AppCompatActivity {
    private final static String TAG = ConnectedActivity.class.getSimpleName();

    private static final Integer DISTANCE_THRESHOLD = 150; // in cm

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String mDeviceName; // current BLE device name
    private String mDeviceAddress; // current BLE device address
    private BluetoothLeService mBluetoothLeService;

    // UI components
    TextView distanceTextView;

    // components to interact
    GpsTracker gpsTracker;
    WarningDao warningDao;

    // current minimal distance of overtaking. null when there is no overtaking taking place.
    Integer currentMinDistance;

    private final static UUID UUID_DISTANCE_CHARACTERISTIC =
            UUID.fromString(GattAttributes.DISTANCE_CHARACTERISTIC);

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                invalidateOptionsMenu();
                finish();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                onDistanceChanged(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        distanceTextView = findViewById(R.id.distanceTextView);

        getSupportActionBar().setTitle(mDeviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        gpsTracker = new GpsTracker(this);
        if(!gpsTracker.canGetLocation()) {
            gpsTracker.showSettingsAlert();
        }

        warningDao = ((DistanceWarnerApplication) this.getApplication()).database.warningDao();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        gpsTracker.stopUsingGPS();

        // write all warnings to database and delete them(for testing purposes)
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Logging and deleting all Warnings");
                for (Warning warning : warningDao.getAll()) {
                    Log.i(TAG, warning.toString());
                    warningDao.delete(warning);
                }
            }
        });
    }

    void onDistanceChanged(String distance) {
        if (distance == null) {
            return;
        }

        final Integer distanceAsInt = Integer.valueOf(distance);
        setDistanceField(distanceAsInt);

        // check if distance is again greater than threshold (this means the car passed already)
        // then save minimum distance of this overtaking
        if (distanceAsInt > DISTANCE_THRESHOLD && currentMinDistance != null) {
            // save location in database
            Log.i(TAG, "Distance violation! lat:" + gpsTracker.getLatitude() + ",long:" + gpsTracker.getLongitude());

            final double lat = gpsTracker.canGetLocation() ? gpsTracker.getLatitude() : 0;
            final double lon = gpsTracker.canGetLocation() ? gpsTracker.getLongitude() : 0;
            final int distanceToSave = currentMinDistance;
            Executors.newCachedThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    warningDao.insertAll(new Warning(System.currentTimeMillis(), lat, lon, distanceToSave));
                }
            });
            currentMinDistance = null;
        } else {
            // set minDistance,
            // when overtaking begins
            // or distance is even smaller
            if (distanceAsInt < DISTANCE_THRESHOLD) {
                if (currentMinDistance == null || distanceAsInt < currentMinDistance) {
                    currentMinDistance = distanceAsInt;
                }
            }
        }
    }

    private void setDistanceField(Integer distance) {
        distanceTextView.setText(distance.toString());

        if (distance < DISTANCE_THRESHOLD) {
            if(getResources() != null)  { // dirty because of unit test
                distanceTextView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryBright));
            }
        } else  {
            distanceTextView.setTextColor(Color.BLACK);
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            Log.i(TAG, "Found GattService: " + gattService.getUuid().toString());

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic characteristic : gattCharacteristics) {
                UUID uuid = characteristic.getUuid();
                Log.i(TAG, "Found characteristic UUID: " + uuid.toString());

                if(UUID_DISTANCE_CHARACTERISTIC.equals(uuid))   {
                    final int charaProp = characteristic.getProperties();
                    //if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    //    mBluetoothLeService.readCharacteristic(characteristic);
                    //}
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mBluetoothLeService.setCharacteristicNotification(
                                characteristic, true);
                    }
                }
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
