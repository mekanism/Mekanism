package mekanism.common.tests.helpers;

import java.util.List;
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

    //TODO - 26.1: Do we need a frequency name here or can we just use a constant when passing to the constructor
    public ParametrizedGameTestSequence<QIOFrequency> startWithFrequency(String frequencyName) {
        return startSequence().thenMap(() -> {
            //TODO - 26.1: Re-evaluate this, but I think this makes the most sense as then we don't persist it so don't have to worry if our test is polluted with previous data
            return new QIOFrequency(frequencyName, Mekanism.gameProfile.id(), SecurityMode.PUBLIC);
        }).thenExecute(frequency -> {
            if (frequency.getTotalItemCount() > 0 || frequency.getTotalItemTypes(false) > 0) {
                fail("Newly created QIO frequency with name '" + frequencyName + "' was not empty");
            }
        });
    }

    public void addDrives(QIOFrequency frequency) {
        TileEntityQIODriveArray driveArray = getBlockEntity(0, 0, 0, TileEntityQIODriveArray.class);
        //TODO - 26.1: Do we need to somehow set the frequency on the BE?
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
        }
        //TODO - 26.1: Validate that the item type is not present in the map anymore (or any of that extra data) after it had been added during the transaction
    }

    public void testExtract(QIOFrequency frequency, ItemResource itemType, long amount, boolean commit) {
        //TODO - 26.1: Is there a better way for us to do this that ends up with having items in the frequency to be able to extract from?
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
            //TODO - 26.1: Validate that the item type is not present in the map anymore (or any of that extra data)
        } else if (stored != amount) {
            fail("Expected frequency to contain: 0 " + itemType + " after rolling back the transaction");
        } else if (frequency.getTotalItemCount() != amount) {
            fail("Expected frequency to have " + amount + " total items after rolling back the transaction");
        }
    }
}