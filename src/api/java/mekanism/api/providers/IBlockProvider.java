package mekanism.api.providers;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("removal")
@MethodsReturnNonnullByDefault
@Deprecated(forRemoval = true, since = "10.7.11")
public interface IBlockProvider extends IItemProvider {

    Block getBlock();

    /**
     * Helper to get the default block state for the provided block.
     *
     * @since 10.5.20
     */
    @NotNull
    default BlockState defaultState() {
        return getBlock().defaultBlockState();
    }

    @Override
    default ResourceLocation getRegistryName() {
        //Make sure to use the block's registry name in case it somehow doesn't match
        return BuiltInRegistries.BLOCK.getKey(getBlock());
    }

    @Override
    default String getTranslationKey() {
        return getBlock().getDescriptionId();
    }

    @Override
    default Component getTextComponent() {
        return getBlock().getName();
    }
}