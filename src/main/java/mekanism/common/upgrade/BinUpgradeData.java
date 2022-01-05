package mekanism.common.upgrade;

import mekanism.common.inventory.slot.BinInventorySlot;

public record BinUpgradeData(boolean redstone, BinInventorySlot binSlot) implements IUpgradeData {
}