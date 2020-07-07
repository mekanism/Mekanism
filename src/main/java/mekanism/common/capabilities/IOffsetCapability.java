package mekanism.common.capabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Allows for handling capabilities at an offset to the actual implementer. This allows Tile Entities such as the Digital Miner to via the advanced bounding blocks be
 * able to return a partial true for hasCapability for a given side. Example: Instead of the entire back side having access to the ItemHandler capability, only the eject
 * slot on the back has access.
 */
public interface IOffsetCapability {//TODO: Eventually we may want to give offset capabilities the CapabilityCache treatment more directly

    /**
     * Retrieves the handler for the capability requested on the specific side with a given offset.
     * <ul>
     * <li>The return value <strong>CAN</strong> be null if the object does not support the capability.</il>
     * <li>The return value <strong>MUST</strong> be null if {@link #isOffsetCapabilityDisabled(Capability, Direction, Vector3i)} is true.</il>
     * <li>The return value <strong>CAN</strong> be the same for multiple faces.</li>
     * </ul>
     *
     * @param capability The capability to check
     * @param side       The Side to check from,
     *                   <strong>CAN BE NULL</strong>. Null is defined to represent 'internal' or 'self'
     * @param offset     An offset position to figure out what block is actually the one that is being checked.
     *
     * @return The requested capability.
     *
     * @implNote Do not override this method if you are implementing {@link IOffsetCapability}, instead override {@link #getOffsetCapabilityIfEnabled(Capability,
     * Direction, Vector3i)}, calling this method is fine.
     */
    @Nonnull
    default <T> LazyOptional<T> getOffsetCapability(@Nonnull Capability<T> capability, @Nullable Direction side, @Nonnull Vector3i offset) {
        return isOffsetCapabilityDisabled(capability, side, offset) ? LazyOptional.empty() : getOffsetCapabilityIfEnabled(capability, side, offset);
    }

    /**
     * Checks if a given capability is disabled for this provider on the given side and offset. If false is returned it makes makes no guarantees that the capability is
     * provided.
     *
     * @param capability The capability to check
     * @param side       The Side to check from: CAN BE NULL. Null is defined to represent 'internal' or 'self'
     * @param offset     An offset position to figure out what block is actually the one that is being checked.
     *
     * @return True if this given capability is disabled for the given side and offset. If true, then {@link #getOffsetCapability(Capability, Direction, Vector3i)} should
     * return {@link LazyOptional#empty()}.
     */
    default boolean isOffsetCapabilityDisabled(@Nonnull Capability<?> capability, @Nullable Direction side, @Nonnull Vector3i offset) {
        return false;
    }

    /**
     * Copy of {@link #getOffsetCapability(Capability, Direction, Vector3i)} but checks for if the capability is disabled before being called. Docs copied for
     * convenience
     *
     * Retrieves the handler for the capability requested on the specific side with a given offset.
     * <ul>
     * <li>The return value <strong>CAN</strong> be null if the object does not support the capability.</il>
     * <li>The return value <strong>MUST</strong> be null if {@link #isOffsetCapabilityDisabled(Capability, Direction, Vector3i)} is true.</il>
     * <li>The return value <strong>CAN</strong> be the same for multiple faces.</li>
     * </ul>
     *
     * @param capability The capability to check
     * @param side       The Side to check from,
     *                   <strong>CAN BE NULL</strong>. Null is defined to represent 'internal' or 'self'
     * @param offset     An offset position to figure out what block is actually the one that is being checked.
     *
     * @return The requested capability.
     */
    @Nonnull
    <T> LazyOptional<T> getOffsetCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side, @Nonnull Vector3i offset);
}