package mekanism.common.block.attribute;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.block.BlockState;

public class AttributeUpgradeable implements Attribute {

    private final Supplier<BlockRegistryObject<?, ?>> upgradeBlock;

    public AttributeUpgradeable(Supplier<BlockRegistryObject<?, ?>> upgradeBlock) {
        this.upgradeBlock = upgradeBlock;
    }

    @Nonnull
    public BlockState upgradeResult(@Nonnull BlockState current, @Nonnull BaseTier tier) {
        return BlockStateHelper.copyStateData(current, upgradeBlock.get().getBlock().getDefaultState());
    }
}
