package mekanism.api.providers;

import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public interface IBlockProvider extends IItemProvider {

    @Nonnull
    Block getBlock();

    @Override
    default ResourceLocation getRegistryName() {
        //Make sure to use the block's registry name in case it somehow doesn't match
        return ForgeRegistries.BLOCKS.getKey(getBlock());
    }

    @Override
    default String getTranslationKey() {
        return getBlock().getDescriptionId();
    }
}