package mekanism.common.tile.interfaces;

import javax.annotation.Nonnull;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.IOffsetCapability;
import mekanism.common.lib.security.ISecurityTile;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Internal interface.  A bounding block is not actually a 'bounding' block, it is really just a fake block that is used to mimic actual block bounds.
 *
 * @author AidanBrady
 */
public interface IBoundingBlock extends ICapabilityProvider, IComparatorSupport, IOffsetCapability, ISecurityTile, IUpgradeTile {

    /**
     * Called when the main block is placed.
     */
    //TODO: Evaluate this, onPlace is a method in TileEntityMekanism now, so it always gets called via that exposure
    // So having this here may not be needed anymore
    void onPlace();

    default void onBoundingBlockPowerChange(BlockPos boundingPos, int oldLevel, int newLevel) {
    }

    default int getBoundingComparatorSignal(Vector3i offset) {
        return 0;
    }

    @Override
    default boolean isOffsetCapabilityDisabled(@Nonnull Capability<?> capability, Direction side, @Nonnull Vector3i offset) {
        //By default, only allow proxying the config card capability to bounding blocks
        return capability != Capabilities.CONFIG_CARD_CAPABILITY;
    }

    @Nonnull
    @Override
    default <T> LazyOptional<T> getOffsetCapabilityIfEnabled(@Nonnull Capability<T> capability, Direction side, @Nonnull Vector3i offset) {
        //And have it get the capability as if it was not offset
        return getCapability(capability, side);
    }
}