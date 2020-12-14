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
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.UUID;

import at.burgr.distancewarner.bluetooth.DistanceMeasurementService;
import at.burgr.distancewarner.bluetooth.GattAttributes;

import static at.burgr.distancewarner.DistanceWarnerApplication.DISTANCE_THRESHOLD;

/**
 * This activity is shown, when we are connected to the descired BLE device. The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class ConnectedActivity extends BaseActivity {
    private final static String TAG = ConnectedActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String mDeviceName; // current BLE device name
    private String mDeviceAddress; // current BLE device address
    private DistanceMeasurementService mDistanceMeasurementService;

    // UI components
    TextView distanceTextView;

    private final static UUID UUID_DISTANCE_CHARACTERISTIC =
            UUID.fromString(GattAttributes.DISTANCE_CHARACTERISTIC);


    @Override
    int getContentViewId() {
        return R.layout.activity_connected;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_connected;
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mDistanceMeasurementService = ((DistanceMeasurementService.LocalBinder) service).getService();
            if (!mDistanceMeasurementService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mDistanceMeasurementService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mDistanceMeasurementService = null;
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
            if (DistanceMeasurementService.ACTION_GATT_CONNECTED.equals(action)) {
                invalidateOptionsMenu();
            } else if (DistanceMeasurementService.ACTION_GATT_DISCONNECTED.equals(action)) {
                invalidateOptionsMenu();
                finish();
            } else if (DistanceMeasurementService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mDistanceMeasurementService.getSupportedGattServices());
            } else if (DistanceMeasurementService.ACTION_DATA_AVAILABLE.equals(action)) {
                setDistanceField(intent.getStringExtra(DistanceMeasurementService.EXTRA_DATA));
            }
        }
    };

    protected void getCreateInActivity(Bundle savedInstanceState) {
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        distanceTextView = findViewById(R.id.distanceTextView);

        getSupportActionBar().setTitle(mDeviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, DistanceMeasurementService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mDistanceMeasurementService != null) {
            final boolean result = mDistanceMeasurementService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mDistanceMeasurementService = null;
    }

    private void setDistanceField(String distance) {
        distanceTextView.setText(distance);

        Integer distanceAsInt = Integer.valueOf(distance);
        if (distanceAsInt < DISTANCE_THRESHOLD) {
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
                        mDistanceMeasurementService.setCharacteristicNotification(
                                characteristic, true);
                    }
                }
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DistanceMeasurementService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(DistanceMeasurementService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(DistanceMeasurementService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(DistanceMeasurementService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
