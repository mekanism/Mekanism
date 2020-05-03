package mekanism.common.content.qio;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.frequency.FrequencyType;
import mekanism.common.inventory.slot.QIODriveSlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.TileEntityQIOComponent;
import net.minecraft.nbt.CompoundNBT;

public class TileEntityQIODriveArray extends TileEntityQIOComponent implements IQIODriveHolder {

    private List<IInventorySlot> driveSlots;

    public TileEntityQIODriveArray() {
        super(MekanismBlocks.QIO_DRIVE_ARRAY);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        final int xSize = 176;
        driveSlots = new ArrayList<>();
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 8; x++) {
                QIODriveSlot slot = new QIODriveSlot(this, y * 8 + x, xSize / 2 - (8 * 18 / 2) + x * 18, 70 + y * 18);
                driveSlots.add(slot);
                builder.addSlot(slot);
            }
        }
        return builder.build();
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        QIOFrequency freq = getFrequency(FrequencyType.QIO);
        if (freq != null) {
            // save all item data before we save
            freq.saveAll();
        }
        super.write(tag);
        return tag;
    }

    @Override
    public void onDataUpdate() {
        markDirty(false);
    }

    @Override
    public List<IInventorySlot> getDriveSlots() {
        return driveSlots;
    }
}
