package mekanism.common.item.gear;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import org.jetbrains.annotations.NotNull;

public abstract class ItemSpecialArmor extends Item {

    protected ItemSpecialArmor(Holder<ArmorMaterial> material, ArmorType armorType, Item.Properties properties) {
        super(properties.humanoidArmor(material.value(), armorType));
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