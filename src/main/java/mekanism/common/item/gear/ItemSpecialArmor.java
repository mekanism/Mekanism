package mekanism.common.item.gear;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public abstract class ItemSpecialArmor extends Item {

    protected ItemSpecialArmor(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return stack.has(DataComponents.ENCHANTABLE) && super.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return stack.has(DataComponents.ENCHANTABLE) && super.supportsEnchantment(stack, enchantment);
    }
}