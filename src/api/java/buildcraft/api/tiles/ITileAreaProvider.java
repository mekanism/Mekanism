package buildcraft.api.tiles;

import net.minecraft.util.BlockPos;

import buildcraft.api.core.IAreaProvider;

/** Used for more fine-grained control of whether or not a machine connects to the provider here. */
public interface ITileAreaProvider extends IAreaProvider {
    boolean isValidFromLocation(BlockPos pos);
}
