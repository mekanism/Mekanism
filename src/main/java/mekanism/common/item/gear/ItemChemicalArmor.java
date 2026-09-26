package mekanism.common.item.gear;

import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.item.interfaces.IChemicalItem;
import mekanism.common.util.StorageUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public abstract class ItemChemicalArmor extends ItemSpecialArmor implements IChemicalItem {

    protected ItemChemicalArmor(Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return StorageUtils.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return StorageUtils.getBarWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ContainerType.CHEMICAL.getRGBDurabilityForDisplay(stack);
    }
}