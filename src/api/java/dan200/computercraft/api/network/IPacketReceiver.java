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
 * An object on an {@link IPacketNetwork}, capable of receiving packets.
 */
public interface IPacketReceiver
{
    /**
     * Get the world in which this packet receiver exists.
     *
     * @return The receivers's world.
     */
    @Nonnull
    World getWorld();

    /**
     * Get the position in the world at which this receiver exists.
     *
     * @return The receiver's position.
     */
    @Nonnull
    Vec3d getPosition();

    /**
     * Get the maximum distance this receiver can send and receive messages.
     *
     * When determining whether a receiver can receive a message, the largest distance of the packet and receiver is
     * used - ensuring it is within range. If the packet or receiver is inter-dimensional, then the packet will always
     * be received.
     *
     * @return The maximum distance this device can send and receive messages.
     * @see #isInterdimensional()
     * @see #receiveSameDimension(Packet packet, double)
     * @see IPacketNetwork#transmitInterdimensional(Packet)
     */
    double getRange();

    /**
     * Determine whether this receiver can receive packets from other dimensions.
     *
     * A device will receive an inter-dimensional packet if either it or the sending device is inter-dimensional.
     *
     * @return Whether this receiver receives packets from other dimensions.
     * @see #getRange()
     * @see #receiveDifferentDimension(Packet)
     * @see IPacketNetwork#transmitInterdimensional(Packet) 
     */
    boolean isInterdimensional();

    /**
     * Receive a network packet from the same dimension.
     *
     * @param packet   The packet to receive. Generally you should check that you are listening on the given channel and,
     *                 if so, queue the appropriate modem event.
     * @param distance The distance this packet has travelled from the source.
     * @see Packet
     * @see #getRange()
     * @see IPacketNetwork#transmitSameDimension(Packet, double)
     * @see IPacketNetwork#transmitInterdimensional(Packet)
     */
    void receiveSameDimension( @Nonnull Packet packet, double distance );

    /**
     * Receive a network packet from a different dimension.
     *
     * @param packet The packet to receive. Generally you should check that you are listening on the given channel and,
     *               if so, queue the appropriate modem event.
     * @see Packet
     * @see IPacketNetwork#transmitInterdimensional(Packet)
     * @see IPacketNetwork#transmitSameDimension(Packet, double)
     * @see #isInterdimensional()
     */
    void receiveDifferentDimension( @Nonnull Packet packet );
}
