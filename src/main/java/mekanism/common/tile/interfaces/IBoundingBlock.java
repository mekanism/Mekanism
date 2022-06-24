package mekanism.common.tile.interfaces;

import java.util.Set;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.IOffsetCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

/**
 * Internal interface.  A bounding block is not actually a 'bounding' block, it is really just a fake block that is used to mimic actual block bounds.
 *
 * @author AidanBrady
 */
public interface IBoundingBlock extends ICapabilityProvider, IComparatorSupport, IOffsetCapability, IUpgradeTile {

    Set<Capability<?>> ALWAYS_PROXY = Set.of(
          Capabilities.CONFIG_CARD,
          Capabilities.OWNER_OBJECT,
          Capabilities.SECURITY_OBJECT
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
    default boolean isOffsetCapabilityDisabled(@NotNull Capability<?> capability, Direction side, @NotNull Vec3i offset) {
        //By default, only allow proxying specific capabilities
        return !ALWAYS_PROXY.contains(capability);
    }

    @NotNull
    @Override
    default <T> LazyOptional<T> getOffsetCapabilityIfEnabled(@NotNull Capability<T> capability, Direction side, @NotNull Vec3i offset) {
        //And have it get the capability as if it was not offset
        return getCapability(capability, side);
    }
}