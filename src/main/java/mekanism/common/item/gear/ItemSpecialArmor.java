package mekanism.common.item.gear;

import mekanism.common.registries.MekanismArmorMaterials;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import org.jetbrains.annotations.NotNull;

public abstract class ItemSpecialArmor extends Item {

    protected ItemSpecialArmor(ArmorMaterial material, ArmorType armorType, Item.Properties properties) {
        super(MekanismArmorMaterials.apply(properties, material, armorType));
    }

    @Override
    public boolean isPrimaryItemFor(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return stack.has(DataComponents.ENCHANTABLE) && super.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    public boolean supportsEnchantment(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return stack.has(DataComponents.ENCHANTABLE) && super.supportsEnchantment(stack, enchantment);
    }
}