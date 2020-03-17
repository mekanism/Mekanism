package mekanism.common.block.attribute;

import javax.annotation.Nonnull;
import mekanism.api.tier.BaseTier;
import mekanism.api.tier.ITier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.block.BlockState;

public class AttributeUpgradeable<TIER extends ITier> implements Attribute {

    private BlockRegistryObject<?, ?> upgradeBlock;

    public AttributeUpgradeable(BlockRegistryObject<?, ?> upgradeBlock) {
        this.upgradeBlock = upgradeBlock;
    }

    @Nonnull
    public BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
        return BlockStateHelper.copyStateData(current, upgradeBlock.getBlock().getDefaultState());
    }
}
