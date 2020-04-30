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
import mekanism.common.tile.base.TileEntityMekanism;

public class TileEntityQIODriveArray extends TileEntityMekanism implements IQIODriveHolder {

    private List<IInventorySlot> driveSlots;

    public TileEntityQIODriveArray() {
        super(MekanismBlocks.QIO_DRIVE_ARRAY);
        frequencyComponent.track(FrequencyType.QIO, true, true, true);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        final int xSize = 176;
        driveSlots = new ArrayList<>();
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 8; x++) {
                QIODriveSlot slot = new QIODriveSlot(this, y * 8 + x, xSize / 2 - (8 * 18 / 2) + x * 18, 40);
                driveSlots.add(slot);
                builder.addSlot(slot);
            }
        }
        return builder.build();
    }

    @Override
    public List<IInventorySlot> getDriveSlots() {
        return driveSlots;
    }
}
