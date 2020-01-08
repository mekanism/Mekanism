package mekanism.common.upgrade;

import mekanism.common.inventory.slot.BinInventorySlot;

public class BinUpgradeData implements IUpgradeData {

    public final boolean redstone;
    public final BinInventorySlot binSlot;

    public BinUpgradeData(boolean redstone, BinInventorySlot binSlot) {
        this.redstone = redstone;
        this.binSlot = binSlot;
    }
}