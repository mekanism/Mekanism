package mekanism.common.item.gear;

import mekanism.common.registries.MekanismArmorMaterials;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;

public class ItemScubaMask extends ItemSpecialArmor {

    public ItemScubaMask(Properties properties) {
        super(MekanismArmorMaterials.SCUBA_MASK, ArmorItem.Type.HELMET, properties.rarity(Rarity.RARE).setNoRepair().stacksTo(1));
    }

    @Override
    public boolean isPrimaryItemFor(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.AQUA_AFFINITY) || super.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    public boolean supportsEnchantment(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.AQUA_AFFINITY) || super.supportsEnchantment(stack, enchantment);
    }
}