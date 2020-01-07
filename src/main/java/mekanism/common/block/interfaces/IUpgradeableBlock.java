package mekanism.common.block.interfaces;

import javax.annotation.Nonnull;
import mekanism.common.tier.BaseTier;
import mekanism.common.tier.ITier;
import net.minecraft.block.BlockState;

//TODO: Do we want to allow blocks that can be upgraded that don't have a tier?
public interface IUpgradeableBlock<TIER extends ITier> extends ITieredBlock<TIER> {

    @Nonnull
    BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier);
}