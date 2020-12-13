package at.burgr.distancewarner;

import android.app.Application;

import at.burgr.distancewarner.data.AppDatabase;

public class DistanceWarnerApplication extends Application {
    public AppDatabase database;
    public  static final Integer DISTANCE_THRESHOLD = 150; // in cm

    @Override
    public void onCreate() {
        super.onCreate();

        database = AppDatabase.getInstance(this);
    }
}
