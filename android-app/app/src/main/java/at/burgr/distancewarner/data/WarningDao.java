package at.burgr.distancewarner.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WarningDao {
    @Query("SELECT * FROM warning")
    List<Warning> getAll();

    @Query("SELECT * FROM warning WHERE timestamp IN (:timestamps)")
    List<Warning> loadAllByIds(int[] timestamps);

    @Insert
    void insertAll(Warning... warning);

    @Delete
    void delete(Warning warning);

}
