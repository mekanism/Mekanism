package mekanism.tools.common.item;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemRefinedGlowstoneArmor extends ItemMekanismArmor {

    public ItemRefinedGlowstoneArmor(Holder<ArmorMaterial> material, ArmorItem.Type armorType, Properties properties) {
        super(material, armorType, properties);
    }

    @Override
    public boolean makesPiglinsNeutral(@NotNull ItemStack stack, @NotNull LivingEntity wearer) {
        return true;
    }
}