package at.burgr.distancewarner;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

/**
 * This activity is shown at start. It requests permissions and scans for the BLE device to connect
 */
public class ScanActivity extends BaseActivity {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    private ProgressBar progressBar;
    private boolean connected;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private final static String TAG = ScanActivity.class.getSimpleName();

    private static final String BLE_DEVICE_NAME = "DistanceSensor";

    @Override
    int getContentViewId() {
        return R.layout.activity_scan;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_scan;
    }

    @Override
    protected void getCreateInActivity(Bundle savedInstanceState) {
        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
        progressBar = findViewById(R.id.progressBar);

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        // Make sure we have access fine location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_FINE_LOCATION);
        }

        startScanning();
        progressBar.setIndeterminate(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopScanning();
        startScanning();
    }

    public void startScanning() {
        Log.i(TAG,"start scanning");
        progressBar.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });
    }

    public void stopScanning() {
        connected = false;
        progressBar.setVisibility(View.GONE);
        Log.i(TAG,"stop scanning");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }

    // Device scan callback.
    private ScanCallback leScanCallback =
            new ScanCallback() {
                public void onScanResult(int callbackType, ScanResult result) {

                    BluetoothDevice device = result.getDevice();
                    Log.i(TAG, "Device Name: " + device.getName()
                            + " rssi: " + result.getRssi()
                            + " uuid: " + device.getUuids()
                            + " address: " + device.getAddress()
                            + "\n");

                    // find device to connect and connect to it
                    if (device.getName() != null) {
                        if(device.getName().equals(BLE_DEVICE_NAME))    {
                            connectToDevice(device);
                        }
                    }
                }

                public void onScanFailed(int errorCode) {
                    TextView connectionStateTextView = findViewById(R.id.connectionStateTextView);
                    connectionStateTextView.setText("error: " + errorCode);
                }
            };

    private void connectToDevice(BluetoothDevice device)  {
        if(!connected)  {
            stopScanning();
            connected = true;

            final Intent intent = new Intent(this, ConnectedActivity.class);
            intent.putExtra(ConnectedActivity.EXTRAS_DEVICE_NAME, device.getName());
            intent.putExtra(ConnectedActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("fine location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }
}
