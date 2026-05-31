package mekanism.common.tile.qio;

import mekanism.common.tile.qio.TileEntityQIODriveArray.DriveStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DriveStatusPackTest {

    @Test
    void testSimpleDriveStatus() {
        long testValue = 0;

        for (int i = 0; i < TileEntityQIODriveArray.DRIVE_SLOTS; i++) {
            Assertions.assertEquals(DriveStatus.NONE, TileEntityQIODriveArray.getStatus(i, testValue), "expected empty as initial value");
        }
        for (int i = 0; i < TileEntityQIODriveArray.DRIVE_SLOTS; i++) {
            testValue = TileEntityQIODriveArray.updateStatus(i, DriveStatus.FULL, testValue);
            Assertions.assertEquals(DriveStatus.FULL, TileEntityQIODriveArray.getStatus(i, testValue), "expected value that was just set");
        }
    }

    @Test
    void testUpdating() {
        long packed = 0;

        //set to full to start with, to check that removing bits works
        DriveStatus startValue = DriveStatus.FULL;
        for (int i = 0; i < TileEntityQIODriveArray.DRIVE_SLOTS; i++) {
            packed = TileEntityQIODriveArray.updateStatus(i, startValue, packed);
        }

        for (int i = 0; i < TileEntityQIODriveArray.DRIVE_SLOTS; i++) {
            Assertions.assertEquals(startValue, TileEntityQIODriveArray.getStatus(i, packed), "expected seed value");
            DriveStatus testItem = DriveStatus.READY;
            packed = TileEntityQIODriveArray.updateStatus(i, testItem, packed);
            Assertions.assertEquals(testItem, TileEntityQIODriveArray.getStatus(i, packed), "expected value that was just set");
            for (int j = 0; j < TileEntityQIODriveArray.DRIVE_SLOTS; j++) {
                if (j ==i) {
                    continue;
                }
                Assertions.assertEquals(startValue, TileEntityQIODriveArray.getStatus(j, packed), "expected seed value in other idx: "+j);
            }
            packed = TileEntityQIODriveArray.updateStatus(i, startValue, packed);
        }
    }
}
