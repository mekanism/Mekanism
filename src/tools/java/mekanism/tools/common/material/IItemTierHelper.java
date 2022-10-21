package mekanism.tools.common.material;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

abstract class IItemTierHelper implements Tier {

    public abstract int getItemEnchantability();

    public abstract Ingredient getItemRepairMaterial();

    /**
     * Wrap the IItemTier methods that have the same deobf name as IArmorMaterial to regular variants so that they can both be resolved as implemented when
     * reobfuscating.
     *
     * @apiNote Both {@link #getItemEnchantability()} and {@link IArmorMaterialHelper#getArmorEnchantability()} are wrapped back into a single method in {@link
     * BaseMekanismMaterial}
     */
    @Override
    public int getEnchantmentValue() {
        return getItemEnchantability();
    }

    /**
     * Wrap the IItemTier methods that have the same deobf name as IArmorMaterial to regular variants so that they can both be resolved as implemented when
     * reobfuscating.
     *
     * @apiNote Both {@link #getItemRepairMaterial()} and {@link IArmorMaterialHelper#getArmorRepairMaterial()} are wrapped back into a single method in {@link
     * BaseMekanismMaterial}
     */
    @NotNull
    @Override
    public Ingredient getRepairIngredient() {
        return getItemRepairMaterial();
    }
}