package mekanism.common.block.attribute;

import java.util.function.Supplier;
import mekanism.api.tier.BaseTier;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.registration.impl.BlockRegistryObject;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class AttributeUpgradeable implements Attribute {

    @Nullable
    private final Supplier<BlockRegistryObject<?, ?>> upgradeBlock;

    public AttributeUpgradeable(@Nullable Supplier<BlockRegistryObject<?, ?>> upgradeBlock) {
        this.upgradeBlock = upgradeBlock;
    }

    public BlockState upgradeResult(BlockState current, BaseTier tier) {
        if (upgradeBlock == null) {
            return current;
        }
        return BlockStateHelper.copyStateData(current, upgradeBlock.get());
    }
}
