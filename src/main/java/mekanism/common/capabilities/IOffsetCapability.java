package mekanism.common.capabilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.capabilities.Capability;

/**
 * Allows for handling capabilities at an offset to the actual implementer. This allows Tile Entities such as the
 * Digital Miner to via the advanced bounding blocks be able to return a partial true for hasCapability for a given
 * side. Example: Instead of the entire back side having access to the ItemHandler capability, only the eject slot on
 * the back has access.
 */
public interface IOffsetCapability extends IToggleableCapability {

    /**
     * Determines if this object has support for the capability in question on the specific side, with a given offset.
     * The return value of this MIGHT change during runtime if this object gains or loses support for a capability. It
     * is not required to call this function before calling {@link #getCapability(Capability, EnumFacing)}.
     *
     * @param capability The capability to check
     * @param side The Side to check from: CAN BE NULL. Null is defined to represent 'internal' or 'self'
     * @param offset An offset position to figure out what block is actually the one that is being checked.
     * @return True if this object supports the capability. If true, then {@link #getCapability(Capability, EnumFacing)}
     * must not return null.
     */
    boolean hasOffsetCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing side, @Nonnull Vec3i offset);

    /**
     * Retrieves the handler for the capability requested on the specific side with a given offset.
     * <ul>
     * <li>The return value <strong>CAN</strong> be null if the object does not support the capability.</il>
     * <li>The return value <strong>MUST</strong> be null if {@link #isOffsetCapabilityDisabled(Capability, EnumFacing,
     * Vec3i)} is true.</il>
     * <li>The return value <strong>CAN</strong> be the same for multiple faces.</li>
     * </ul>
     *
     * @param capability The capability to check
     * @param side The Side to check from,
     * <strong>CAN BE NULL</strong>. Null is defined to represent 'internal' or 'self'
     * @param offset An offset position to figure out what block is actually the one that is being checked.
     * @return The requested capability. Must <strong>NOT</strong> be null when {@link #hasCapability(Capability,
     * EnumFacing)} would return true.
     */
    @Nullable
    <T> T getOffsetCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing side, @Nonnull Vec3i offset);

    /**
     * Checks if a given capability is disabled for this provider on the given side and offset. If false is returned it
     * makes makes no guarantees that the capability is provided.
     *
     * @param capability The capability to check
     * @param side The Side to check from: CAN BE NULL. Null is defined to represent 'internal' or 'self'
     * @param offset An offset position to figure out what block is actually the one that is being checked.
     * @return True if this given capability is disabled for the given side and offset. If true, then {@link
     * #hasOffsetCapability(Capability, EnumFacing, Vec3i)} should return false.
     */
    default boolean isOffsetCapabilityDisabled(@Nonnull Capability<?> capability, @Nullable EnumFacing side,
          @Nonnull Vec3i offset) {
        return false;
    }
}