/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2017. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.api.network;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a packet which may be sent across a {@link IPacketNetwork}.
 *
 * @see IPacketSender
 * @see IPacketNetwork#transmitSameDimension(Packet, double)
 * @see IPacketNetwork#transmitInterdimensional(Packet)
 * @see IPacketReceiver#receiveDifferentDimension(Packet)
 * @see IPacketReceiver#receiveSameDimension(Packet, double)
 */
public class Packet
{
    private final int m_channel;
    private final int m_replyChannel;
    private final Object m_payload;

    private final IPacketSender m_sender;

    /**
     * Create a new packet, ready for transmitting across the network.
     *
     * @param channel      The channel to send the packet along. Receiving devices should only process packets from on
     *                     channels they are listening to.
     * @param replyChannel The channel to reply on.
     * @param payload      The contents of this packet. This should be a "valid" Lua object, safe for queuing as an
     *                     event or returning from a peripheral call.
     * @param sender       The object which sent this packet.
     */
    public Packet( int channel, int replyChannel, @Nullable Object payload, @Nonnull IPacketSender sender )
    {
        Preconditions.checkNotNull( sender, "sender cannot be null" );

        m_channel = channel;
        m_replyChannel = replyChannel;
        m_payload = payload;
        m_sender = sender;
    }

    /**
     * Get the channel this packet is sent along. Receivers should generally only process packets from on channels they
     * are listening to.
     *
     * @return This packet's channel.
     */
    public int getChannel()
    {
        return m_channel;
    }

    /**
     * The channel to reply on. Objects which will reply should send it along this channel.
     *
     * @return This channel to reply on.
     */
    public int getReplyChannel()
    {
        return m_replyChannel;
    }

    /**
     * The actual data of this packet. This should be a "valid" Lua object, safe for queuing as an
     * event or returning from a peripheral call.
     *
     * @return The packet's payload
     */
    @Nullable
    public Object getPayload()
    {
        return m_payload;
    }

    /**
     * The object which sent this message.
     *
     * @return The sending object.
     */
    @Nonnull
    public IPacketSender getSender()
    {
        return m_sender;
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o ) return true;
        if( o == null || getClass() != o.getClass() ) return false;

        Packet packet = (Packet) o;

        if( m_channel != packet.m_channel ) return false;
        if( m_replyChannel != packet.m_replyChannel ) return false;
        if( m_payload != null ? !m_payload.equals( packet.m_payload ) : packet.m_payload != null ) return false;
        return m_sender.equals( packet.m_sender );
    }

    @Override
    public int hashCode()
    {
        int result;
        result = m_channel;
        result = 31 * result + m_replyChannel;
        result = 31 * result + (m_payload != null ? m_payload.hashCode() : 0);
        result = 31 * result + m_sender.hashCode();
        return result;
    }
}
