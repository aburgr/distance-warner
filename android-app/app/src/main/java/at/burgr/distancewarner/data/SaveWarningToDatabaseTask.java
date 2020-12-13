package at.burgr.distancewarner.data;

import android.os.AsyncTask;

public class SaveWarningToDatabaseTask extends AsyncTask<Warning, Void, Void> {

    private final WarningDao dao;

    public SaveWarningToDatabaseTask(WarningDao dao)  {
        this.dao = dao;
    }

    @Override
    protected Void doInBackground(Warning... objects) {
        dao.insertAll(objects);
        return null;
    }
}
