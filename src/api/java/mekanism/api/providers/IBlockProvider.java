package mekanism.api.providers;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

@MethodsReturnNonnullByDefault
public interface IBlockProvider extends IItemProvider {

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