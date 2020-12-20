package at.burgr.distancewarner.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Warning.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WarningDao warningDao();

    private static final String DB_NAME = "distance-warner.db";
    private static AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DB_NAME)
                            .allowMainThreadQueries() // not good practise, but ok for this small database
                            .build();
                }
            }
        }
        return instance;
    }
}
