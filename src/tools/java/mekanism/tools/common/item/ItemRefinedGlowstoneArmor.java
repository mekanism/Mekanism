package mekanism.tools.common.item;

import mekanism.tools.common.material.MaterialCreator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import org.jetbrains.annotations.NotNull;

public class ItemRefinedGlowstoneArmor extends ItemMekanismArmor {

    public ItemRefinedGlowstoneArmor(MaterialCreator material, ArmorType armorType, Item.Properties properties) {
        super(material, armorType, properties);
    }

    @Override
    public boolean makesPiglinsNeutral(@NotNull ItemStack stack, @NotNull LivingEntity wearer) {
        return true;
    }
}