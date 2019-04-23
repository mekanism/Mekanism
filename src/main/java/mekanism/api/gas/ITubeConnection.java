package mekanism.api.gas;

import net.minecraft.util.EnumFacing;

/**
 * Implement this if your block can connect to Pressurized Tubes.
 *
 * @author AidanBrady
 * @deprecated Dropped in favor of better support for machines of what implements GAS_HANDLER_CAPABILITY use {@link
 * IGasHandler} instead.
 */
@Deprecated
public interface ITubeConnection {

    /**
     * Whether or not a tube can connect to a certain orientation.
     *
     * @param side - orientation to check
     * @return if a tube can connect
     */
    boolean canTubeConnect(EnumFacing side);
}
