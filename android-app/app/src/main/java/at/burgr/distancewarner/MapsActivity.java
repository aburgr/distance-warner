// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package at.burgr.distancewarner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import at.burgr.distancewarner.data.AppDatabase;
import at.burgr.distancewarner.data.Warning;
import at.burgr.distancewarner.data.WarningDao;
import at.burgr.distancewarner.gps.GpsTracker;

/**
 * An activity that displays a Google map with markers to all stored warnings
 */
// [START maps_marker_on_map_ready]
public class MapsActivity extends BaseActivity
        implements OnMapReadyCallback {

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;

    private GpsTracker gpsTracker;
    private WarningDao warningDao;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");

    @Override
    int getContentViewId() {
        return R.layout.activity_maps;
    }

    @Override
    int getNavigationMenuItemId() {
        return R.id.navigation_maps;
    }

    protected void getCreateInActivity(Bundle savedInstanceState) {
        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        gpsTracker = new GpsTracker(this);
        warningDao = AppDatabase.getInstance(this).warningDao();
    }

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user receives a prompt to install
     * Play services inside them SupportMapFragment. The API invokes this method after the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        List<Warning> all = warningDao.getAll();

        for(Warning warning : all)  {
            LatLng warningLocation = new LatLng(warning.latitude, warning.longitude);
            googleMap.addMarker(new MarkerOptions()
                    .position(warningLocation)
                    .title(dateFormat.format(new Date(warning.timestamp)) + ": " + warning.distance + getResources().getText(R.string.distanceUnit)));
        }

        LatLng currentPosition = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
        googleMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.builder()
                            .zoom(17)
                            .target(currentPosition).build()));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_FINE_LOCATION);
            requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, PERMISSION_REQUEST_COARSE_LOCATION);
        }
        googleMap.setMyLocationEnabled(true);
    }
}
