package mekanism.common.block.interfaces;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.tier.BaseTier;
import net.minecraft.block.BlockState;

public interface IUpgradeableBlock {

    @Nonnull
    BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier);

    @Nullable
    default BaseTier getBaseTier() {
        return this instanceof ITieredBlock<?> ? ((ITieredBlock<?>) this).getTier().getBaseTier() : null;
    }
}