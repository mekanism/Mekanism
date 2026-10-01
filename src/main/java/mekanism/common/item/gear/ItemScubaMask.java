package mekanism.common.item.gear;

import mekanism.common.registries.MekanismEquipmentAssets;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.Equippable;

public class ItemScubaMask extends ItemSpecialArmor {

    public ItemScubaMask(Item.Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE)
              //Same enchantment value as iron and turtle
              .enchantable(9)
              .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setAsset(MekanismEquipmentAssets.SCUBA_GEAR).build())
        );
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.AQUA_AFFINITY) || super.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.AQUA_AFFINITY) || super.supportsEnchantment(stack, enchantment);
    }
}