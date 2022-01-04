package mekanism.common.item.gear;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.ItemStack;

public class ItemHDPEElytra extends ElytraItem {

    public ItemHDPEElytra(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.CHEST;
    }

    @Override
    public boolean isValidRepairItem(@Nonnull ItemStack toRepair, ItemStack repair) {
        return repair.getItem() == MekanismItems.HDPE_SHEET.asItem();
    }
}