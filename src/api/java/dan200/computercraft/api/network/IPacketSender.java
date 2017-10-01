/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.api.network;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * An object on a {@link IPacketNetwork}, capable of sending packets.
 */
public interface IPacketSender
{
    /**
     * Get the world in which this packet sender exists.
     *
     * @return The sender's world.
     */
    @Nonnull
    World getWorld();

    /**
     * Get the position in the world at which this sender exists.
     *
     * @return The sender's position.
     */
    @Nonnull
    Vec3d getPosition();

    /**
     * Get some sort of identification string for this sender. This does not strictly need to be unique, but you
     * should be able to extract some identifiable information from it.
     *
     * @return This device's id.
     */
    @Nonnull
    String getSenderID();
}
