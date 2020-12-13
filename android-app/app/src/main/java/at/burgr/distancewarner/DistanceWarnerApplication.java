package at.burgr.distancewarner;

import android.app.Application;

import at.burgr.distancewarner.data.AppDatabase;

public class DistanceWarnerApplication extends Application {
    public AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();

        database = AppDatabase.getInstance(this);
    }
}
