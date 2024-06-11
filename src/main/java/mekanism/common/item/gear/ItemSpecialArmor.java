package mekanism.common.item.gear;

import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.integration.gender.GenderCapabilityHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;

public abstract class ItemSpecialArmor extends ArmorItem implements ICapabilityAware {

    //TODO - 1.21: Override getDefaultAttributeModifiers for the ones that actually have config based stuff??
    protected ItemSpecialArmor(Holder<ArmorMaterial> material, ArmorItem.Type armorType, Properties properties) {
        super(material, armorType, properties);
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return material.value().enchantmentValue() > 0 && super.isEnchantable(stack);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return isEnchantable(stack) && super.isBookEnchantable(stack, book);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return isEnchantable(stack) && super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        GenderCapabilityHelper.addGenderCapability(event, this);
    }
}