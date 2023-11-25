package mekanism.common.tile.interfaces;

import java.util.Set;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.IOffsetCapability;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Internal interface.  A bounding block is not actually a 'bounding' block, it is really just a fake block that is used to mimic actual block bounds.
 *
 * @author AidanBrady
 */
public interface IBoundingBlock extends IComparatorSupport, IOffsetCapability, IUpgradeTile {//TODO - 1.20.2: Figure this out

    //Note: Being moved to Capabilities#registerCapabilities
    Set<BlockCapability<?, @Nullable Direction>> ALWAYS_PROXY = Set.of(
          Capabilities.CONFIG_CARD,
          Capabilities.OWNER_OBJECT.block(),
          Capabilities.SECURITY_OBJECT.block()
    );

    default void onBoundingBlockPowerChange(BlockPos boundingPos, int oldLevel, int newLevel) {
    }

    default int getBoundingComparatorSignal(Vec3i offset) {
        return 0;
    }

    default boolean triggerBoundingEvent(Vec3i offset, int id, int param) {
        return false;
    }

    @Override
    default boolean isOffsetCapabilityDisabled(@NotNull BlockCapability<?, @Nullable Direction> capability, Direction side, @NotNull Vec3i offset) {
        //By default, only allow proxying specific capabilities
        return !ALWAYS_PROXY.contains(capability);
    }

    @Nullable
    @Override
    default <T> T getOffsetCapabilityIfEnabled(@NotNull BlockCapability<T, @Nullable Direction> capability, Direction side, @NotNull Vec3i offset) {
        //And have it get the capability as if it was not offset
        if (this instanceof BlockEntity be) {//TODO: Implement this better
            return WorldUtils.getCapability(be.getLevel(), capability, be.getBlockPos(), null, be, side);
        }
        return null;
    }
}