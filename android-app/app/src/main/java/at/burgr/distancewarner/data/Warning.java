package at.burgr.distancewarner.data;

import android.location.Location;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;
import java.util.Objects;

@Entity
public class Warning {
    @PrimaryKey
    public long timestamp;

    public double latitude;

    public double longitude;

    public int distance;

    public Warning(long timestamp, double latitude, double longitude, int distance) {
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "Warning{" +
                "timestamp=" + new Timestamp(timestamp) +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", distance=" + distance +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Warning warning = (Warning) o;
        return timestamp == warning.timestamp &&
                Double.compare(warning.latitude, latitude) == 0 &&
                Double.compare(warning.longitude, longitude) == 0 &&
                distance == warning.distance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, latitude, longitude, distance);
    }
}
