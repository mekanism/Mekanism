package mekanism.common.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.ITier;
import net.minecraft.item.ItemStack;

public interface ITieredItem<TIER extends ITier> {

    @Nullable
    default BaseTier getBaseTier(@Nonnull ItemStack stack) {
        TIER tier = getTier(stack);
        return tier == null ? null : tier.getBaseTier();
    }

    @Nullable
    TIER getTier(@Nonnull ItemStack stack);
}