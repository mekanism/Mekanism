package mekanism.common.item.gear;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.integration.gender.GenderCapabilityHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;

public abstract class ItemSpecialArmor extends ArmorItem {

    protected ItemSpecialArmor(ArmorMaterial material, EquipmentSlot slot, Properties properties) {
        super(material, slot, properties);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "mekanism:render/null_armor.png";
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return material.getEnchantmentValue() > 0 && super.isEnchantable(stack);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return isEnchantable(stack) && super.isBookEnchantable(stack, book);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return isEnchantable(stack) && super.canApplyAtEnchantingTable(stack, enchantment);
    }

    protected void gatherCapabilities(List<ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
        GenderCapabilityHelper.addGenderCapability(this, capabilities::add);
    }

    @Override
    public final ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        List<ItemCapability> capabilities = new ArrayList<>();
        gatherCapabilities(capabilities, stack, nbt);
        if (capabilities.isEmpty()) {
            return super.initCapabilities(stack, nbt);
        }
        return new ItemCapabilityWrapper(stack, capabilities.toArray(ItemCapability[]::new));
    }
}