package mekanism.api.providers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.FactoryType;
import mekanism.api.block.IHasFactoryType;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

public interface IBlockProvider extends IItemProvider {

    @Nonnull
    Block getBlock();

    @Override
    default ResourceLocation getRegistryName() {
        //Make sure to use the block's registry name in case it somehow doesn't match
        return getBlock().getRegistryName();
    }

    @Override
    default String getTranslationKey() {
        return getBlock().getTranslationKey();
    }

    @Nullable
    default FactoryType getFactoryType() {
        Block block = getBlock();
        if (block instanceof IHasFactoryType) {
            return ((IHasFactoryType) block).getFactoryType();
        }
        return null;
    }
}