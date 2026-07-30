package mekanism.common.item.gear;

import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.item.interfaces.IChemicalItem;
import mekanism.common.util.StorageUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public abstract class ItemChemicalArmor extends ItemSpecialArmor implements IChemicalItem {

    protected ItemChemicalArmor(ArmorMaterial material, ArmorType armorType, Item.Properties properties) {
        super(material, armorType, properties.rarity(Rarity.RARE).setNoCombineRepair().stacksTo(1));
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