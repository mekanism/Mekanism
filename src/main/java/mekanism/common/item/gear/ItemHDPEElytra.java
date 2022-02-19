package mekanism.common.item.gear;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.registries.MekanismItems;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;

public class ItemHDPEElytra extends ElytraItem {

    public ItemHDPEElytra(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public EquipmentSlotType getEquipmentSlot(ItemStack stack) {
        return EquipmentSlotType.CHEST;
    }

    @Override
    public boolean isValidRepairItem(@Nonnull ItemStack toRepair, ItemStack repair) {
        return repair.getItem() == MekanismItems.HDPE_SHEET.getItem();
    }
}