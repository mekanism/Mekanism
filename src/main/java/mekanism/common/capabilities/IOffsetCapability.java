package mekanism.common.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Allows for handling capabilities at an offset to the actual implementer. This allows Tile Entities such as the Digital Miner to via the advanced bounding blocks be
 * able to return a partial true for hasCapability for a given side. Example: Instead of the entire back side having access to the ItemHandler capability, only the eject
 * slot on the back has access.
 */
public interface IOffsetCapability {//TODO: Eventually we may want to give offset capabilities the CapabilityCache treatment more directly

    /**
     * Retrieves the handler for the capability requested on the specific side with a given offset.
     * <ul>
     * <li>The return value <strong>CAN</strong> be null if the object does not support the capability.</li>
     * <li>The return value <strong>MUST</strong> be null if {@link #isOffsetCapabilityDisabled(BlockCapability, Direction, Vec3i)} is true.</li>
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
     * @implNote Do not override this method if you are implementing {@link IOffsetCapability}, instead override
     * {@link #getOffsetCapabilityIfEnabled(BlockCapability, Direction, Vec3i)}, calling this method is fine.
     */
    @Nullable
    default <T> T getOffsetCapability(@NotNull BlockCapability<T, @Nullable Direction> capability, @Nullable Direction side, @NotNull Vec3i offset) {
        return isOffsetCapabilityDisabled(capability, side, offset) ? null : getOffsetCapabilityIfEnabled(capability, side, offset);
    }

    /**
     * Checks if a given capability is disabled for this provider on the given side and offset. If false is returned it makes no guarantees that the capability is
     * provided.
     *
     * @param capability The capability to check
     * @param side       The Side to check from: CAN BE NULL. Null is defined to represent 'internal' or 'self'
     * @param offset     An offset position to figure out what block is actually the one that is being checked.
     *
     * @return True if this given capability is disabled for the given side and offset. If true, then {@link #getOffsetCapability(BlockCapability, Direction, Vec3i)}
     * should return {@code null}.
     */
    default boolean isOffsetCapabilityDisabled(@NotNull BlockCapability<?, @Nullable Direction> capability, @Nullable Direction side, @NotNull Vec3i offset) {
        return false;
    }

    /**
     * Copy of {@link #getOffsetCapability(BlockCapability, Direction, Vec3i)} but checks for if the capability is disabled before being called. Docs copied for
     * convenience
     * <p>
     * Retrieves the handler for the capability requested on the specific side with a given offset.
     * <ul>
     * <li>The return value <strong>CAN</strong> be null if the object does not support the capability.</li>
     * <li>The return value <strong>MUST</strong> be null if {@link #isOffsetCapabilityDisabled(BlockCapability, Direction, Vec3i)} is true.</li>
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
    @Nullable
    <T> T getOffsetCapabilityIfEnabled(@NotNull BlockCapability<T, @Nullable Direction> capability, @Nullable Direction side, @NotNull Vec3i offset);
}