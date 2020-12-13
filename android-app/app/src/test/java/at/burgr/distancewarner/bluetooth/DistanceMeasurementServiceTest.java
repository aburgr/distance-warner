package at.burgr.distancewarner.bluetooth;

import android.widget.TextView;

import org.junit.Test;

import at.burgr.distancewarner.bluetooth.DistanceMeasurementService;
import at.burgr.distancewarner.data.Warning;
import at.burgr.distancewarner.data.WarningDao;
import at.burgr.distancewarner.gps.GpsTracker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DistanceMeasurementServiceTest {
    @Test
    public void distanceChangedSunshine() {
        DistanceMeasurementService testObj = createTestObject();

        testObj.onDistanceChanged(300);
        testObj.onDistanceChanged(50);
        testObj.onDistanceChanged(70);
        assertEquals(Integer.valueOf(50), testObj.currentMinDistance);
        testObj.onDistanceChanged(300);
        assertEquals(null, testObj.currentMinDistance);

        verify(testObj.warningDao, timeout(5000).times(1)).insertAll(any(Warning.class));
    }

    @Test
    public void distanceChangedNoOvertaking() {
        DistanceMeasurementService testObj = createTestObject();

        testObj.onDistanceChanged(300);
        testObj.onDistanceChanged(250);
        testObj.onDistanceChanged(300);
        assertEquals(null, testObj.currentMinDistance);

        verify(testObj.warningDao, times(0)).insertAll(any(Warning.class));
    }

    @Test
    public void distanceChangedTwoOvertakings() throws InterruptedException {
        DistanceMeasurementService testObj = createTestObject();

        testObj.onDistanceChanged(300);
        assertEquals(null, testObj.currentMinDistance);
        testObj.onDistanceChanged(50);
        assertEquals(Integer.valueOf(50), testObj.currentMinDistance);
        testObj.onDistanceChanged(300);
        assertEquals(null, testObj.currentMinDistance);
        testObj.onDistanceChanged(50);
        assertEquals(Integer.valueOf(50), testObj.currentMinDistance);
        testObj.onDistanceChanged(300);
        assertEquals(null, testObj.currentMinDistance);

        verify(testObj.warningDao, timeout(5000).times(2)).insertAll(any(Warning.class));
    }

    private DistanceMeasurementService createTestObject()    {
        DistanceMeasurementService testObj = new DistanceMeasurementService();
        testObj.gpsTracker = mock(GpsTracker.class);
        testObj.warningDao = mock(WarningDao.class);

        when(testObj.gpsTracker.canGetLocation()).thenReturn(true);
        when(testObj.gpsTracker.getLatitude()).thenReturn(1d);
        when(testObj.gpsTracker.getLongitude()).thenReturn(2d);
        return testObj;
    }
}