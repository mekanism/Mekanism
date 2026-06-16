package mekanism.common.tests.helpers;

import java.util.List;
import java.util.Set;
import mekanism.api.security.SecurityMode;
import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIODriveData.QIODriveKey;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.slot.QIODriveSlot;
import mekanism.common.tile.qio.TileEntityQIODriveArray;
import net.minecraft.gametest.framework.GameTestInfo;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.gametest.ParametrizedGameTestSequence;

public class QIOGameTestHelper extends MekGameTestHelper {

    public QIOGameTestHelper(GameTestInfo info) {
        super(info);
    }

    public ParametrizedGameTestSequence<QIOFrequency> startWithFrequency() {
        return startSequence().thenMap(() -> {
            //Note: Create a new frequency that isn't added to the frequency manager to ensure we don't persist it
            QIOFrequency frequency = new QIOFrequency("test_frequency", Mekanism.gameProfile.id(), SecurityMode.PUBLIC);
            frequency.setRegistries(getLevel().registryAccess());
            return frequency;
        }).thenExecute(frequency -> {
            if (frequency.getTotalItemCount() > 0 || frequency.getTotalItemTypes(false) > 0) {
                fail("Newly created QIO frequency was not empty");
            }
        });
    }

    public void addDrives(QIOFrequency frequency) {
        TileEntityQIODriveArray driveArray = getBlockEntity(0, 0, 0, TileEntityQIODriveArray.class);
        //TODO - 26.2: Do we need to somehow set the frequency on the BE? Maybe for testing modifications to the drive slots
        List<QIODriveSlot> driveSlots = driveArray.getDriveSlots();
        for (int slot = 0, size = driveSlots.size(); slot < size; slot++) {
            QIODriveSlot driveSlot = driveSlots.get(slot);
            if (!driveSlot.isEmpty()) {
                frequency.addDrive(new QIODriveKey(driveArray, slot), driveSlot.resource());
            }
        }
    }

    public void testInsert(QIOFrequency frequency, ItemResource itemType, long amount, boolean commit) {
        try (Transaction transaction = Transaction.openRoot()) {
            long inserted = frequency.massInsert(itemType, amount, transaction);
            if (inserted < amount) {
                fail("Expected frequency to accept: " + amount + " " + itemType + ", only accepted: " + inserted);
            } else if (frequency.getStored(itemType) != amount) {
                fail("Expected frequency to contain: " + amount + " " + itemType);
            }
            if (commit) {
                transaction.commit();
            }
        }
        long stored = frequency.getStored(itemType);
        if (commit) {
            if (stored != amount) {
                fail("Expected frequency to contain: " + amount + " " + itemType + " after committing");
            }
        } else if (stored > 0) {
            fail("Expected frequency to contain: 0 " + itemType + " after rolling back the transaction");
        } else if (frequency.getTotalItemCount() > 0) {
            fail("Expected frequency to have zero total items after rolling back the transaction");
        } else {
            //Validate that the item type is not present in amy of the internal caches
            assertNotCached(frequency, itemType, 0);
        }
    }

    public void testExtract(QIOFrequency frequency, ItemResource itemType, long amount, boolean commit) {
        //TODO - 26.2: Is there a better way for us to do this that ends up with having items in the frequency to be able to extract from?
        testInsert(frequency, itemType, amount, true);
        try (Transaction transaction = Transaction.openRoot()) {
            long extracted = frequency.massExtract(itemType, amount, transaction);
            if (extracted < amount) {
                fail("Expected frequency to provide: " + amount + " " + itemType + ", only provided: " + extracted);
            } else if (frequency.getStored(itemType) > 0) {
                fail("Expected frequency to contain: " + amount + " " + itemType);
            }
            if (commit) {
                transaction.commit();
            }
        }
        long stored = frequency.getStored(itemType);
        if (commit) {
            if (stored > 0) {
                fail("Expected frequency to contain: 0 " + itemType + " after committing");
            }
            //Validate that the item type is not present in amy of the internal caches anymore
            assertNotCached(frequency, itemType, 0);
        } else if (stored != amount) {
            fail("Expected frequency to contain: 0 " + itemType + " after rolling back the transaction");
        } else if (frequency.getTotalItemCount() != amount) {
            fail("Expected frequency to have " + amount + " total items after rolling back the transaction");
        }
    }

    public void assertHasCache(QIOFrequency frequency, ItemResource itemType, int assertCall) {
        assertTrue(frequency.anyCacheExists(itemType), "Expected frequency cache types to contain " + itemType + ". Call time: " + assertCall);
    }

    public void assertNotCached(QIOFrequency frequency, ItemResource itemType, int assertCall) {
        assertFalse(frequency.anyCacheExists(itemType), "Expected frequency cache types to not contain " + itemType + ". Call time: " + assertCall);
    }

    public void assertHasDriveCache(QIOFrequency frequency, Set<QIODriveKey> driveKeys, ItemResource itemType, int assertCall) {
        assertTrue(frequency.anyDriveCacheExists(driveKeys, itemType), "Expected frequency drive cache to contain an entry for " + itemType + ". Call time: " + assertCall);
    }

    public void assertNoDriveCache(QIOFrequency frequency, Set<QIODriveKey> driveKeys, ItemResource itemType, int assertCall) {
        assertFalse(frequency.anyDriveCacheExists(driveKeys, itemType), "Expected frequency drive cache to not contain an entry for " + itemType + ". Call time: " + assertCall);
    }

    public void assertHasDriveEmpty(QIOFrequency frequency, Set<QIODriveKey> driveKeys, ItemResource itemType, int assertCall) {
        assertTrue(frequency.anyDriveCachedEmpty(driveKeys, itemType), "Expected frequency drive cache to contain an empty entry for " + itemType + ". Call time: " + assertCall);
    }

    public void assertNoDriveEmpty(QIOFrequency frequency, Set<QIODriveKey> driveKeys, ItemResource itemType, int assertCall) {
        assertFalse(frequency.anyDriveCachedEmpty(driveKeys, itemType), "Expected frequency drive cache to not contain an empty entry for " + itemType + ". Call time: " + assertCall);
    }
}