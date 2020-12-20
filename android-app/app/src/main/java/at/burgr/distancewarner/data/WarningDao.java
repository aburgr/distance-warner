package at.burgr.distancewarner.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WarningDao {
    @Query("SELECT * FROM warning order by timestamp desc")
    List<Warning> getAll();

    @Insert
    void insertAll(Warning... warning);

    @Delete
    void delete(Warning warning);

    @Query("DELETE FROM warning")
    void deleteAll();
}
