package mekanism.common.tile.interfaces;

import mekanism.common.capabilities.IOffsetCapability;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.redstone.Redstone;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal interface.  A bounding block is not actually a 'bounding' block, it is really just a fake block that is used to mimic actual block bounds.
 *
 * @author AidanBrady
 */
public interface IBoundingBlock extends IComparatorSupport, IOffsetCapability, IUpgradeTile {

    default void onBoundingBlockPowerChange(BlockPos boundingPos, int oldLevel, int newLevel) {
    }

    default int getBoundingComparatorSignal(Vec3i offset) {
        return Redstone.SIGNAL_NONE;
    }

    default boolean triggerBoundingEvent(Vec3i offset, int id, int param) {
        return false;
    }

    @Override
    default boolean isOffsetCapabilityDisabled(@NotNull BlockCapability<?, @Nullable Direction> capability, Direction side, @NotNull Vec3i offset) {
        //By default, don't allow proxying any capabilities and instead require this to be overridden
        // Some will always be proxied such as owner and security caps bypassing this entirely
        return true;
    }

    @Nullable
    @Override
    default <T> T getOffsetCapabilityIfEnabled(@NotNull BlockCapability<T, @Nullable Direction> capability, Direction side, @NotNull Vec3i offset) {
        //And have it get the capability as if it was not offset
        return this instanceof BlockEntity be ? WorldUtils.getCapability(be.getLevel(), capability, be.getBlockPos(), null, be, side) : null;
    }
}