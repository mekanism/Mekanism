package mekanism.common.item.gear;

import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.capabilities.radiation.item.RadiationShieldingHandler;
import mekanism.common.registries.MekanismArmorMaterials;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;

public class ItemHazmatSuitArmor extends ArmorItem implements ICapabilityAware {

    public ItemHazmatSuitArmor(Type armorType, Properties properties) {
        super(MekanismArmorMaterials.HAZMAT, armorType, properties.rarity(Rarity.UNCOMMON).stacksTo(1));
    }

    public static double getShieldingByArmor(Type type) {
        return switch (type) {
            case HELMET -> 0.25;
            case CHESTPLATE -> 0.4;
            case LEGGINGS -> 0.2;
            case BOOTS -> 0.15;
            case BODY -> 0.0;
        };
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.RADIATION_SHIELDING, (stack, ctx) -> RadiationShieldingHandler.create(getShieldingByArmor(getType())), this);
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return material.value().enchantmentValue() > 0 && stack.getMaxStackSize() == 1;
    }

    @Override
    public boolean isBookEnchantable(@NotNull ItemStack stack, @NotNull ItemStack book) {
        return isEnchantable(stack) && super.isBookEnchantable(stack, book);
    }

    @Override
    public boolean isPrimaryItemFor(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return isEnchantable(stack) && super.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    public boolean supportsEnchantment(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return isEnchantable(stack) && super.supportsEnchantment(stack, enchantment);
    }
}
