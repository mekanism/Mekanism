package mekanism.tools.common.material;

import javax.annotation.Nonnull;
import net.minecraft.item.IItemTier;
import net.minecraft.item.crafting.Ingredient;

abstract class IItemTierHelper implements IItemTier {

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
    public int getEnchantability() {
        return getItemEnchantability();
    }

    /**
     * Wrap the IItemTier methods that have the same deobf name as IArmorMaterial to regular variants so that they can both be resolved as implemented when
     * reobfuscating.
     *
     * @apiNote Both {@link #getItemRepairMaterial()} and {@link IArmorMaterialHelper#getArmorRepairMaterial()} are wrapped back into a single method in {@link
     * BaseMekanismMaterial}
     */
    @Nonnull
    @Override
    public Ingredient getRepairMaterial() {
        return getItemRepairMaterial();
    }
}