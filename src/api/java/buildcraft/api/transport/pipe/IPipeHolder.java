package buildcraft.api.transport.pipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;

import buildcraft.api.statements.containers.IRedstoneStatementContainer;
import buildcraft.api.transport.IWireManager;
import buildcraft.api.transport.pluggable.PipePluggable;

/** Designates a tile that can contain a pipe, up to 6 sided pluggables. */
public interface IPipeHolder extends IRedstoneStatementContainer {
    World getPipeWorld();

    BlockPos getPipePos();

    TileEntity getPipeTile();

    IPipe getPipe();

    @Nullable
    PipePluggable getPluggable(EnumFacing side);

    @Nullable
    TileEntity getNeighbourTile(EnumFacing side);

    @Nullable
    IPipe getNeighbourPipe(EnumFacing side);

    /** Gets the given capability going outwards from the pipe. This will test the
     * {@link PipePluggable#getInternalCapability(Capability)} first, and the look at the neighbouring tile. */
    @Nullable
    <T> T getCapabilityFromPipe(EnumFacing side, @Nonnull Capability<T> capability);

    IWireManager getWireManager();

    GameProfile getOwner();

    /** @return True if at least 1 handler received this event, false if not. */
    boolean fireEvent(PipeEvent event);

    void scheduleRenderUpdate();

    /** @param parts The parts that want to send a network update. */
    void scheduleNetworkUpdate(PipeMessageReceiver... parts);

    /** Schedules a GUI network update, that is only the players who currently have a pipe element open in a GUI will be
     * updated.
     * 
     * @param parts The parts that want to send a network update. */
    void scheduleNetworkGuiUpdate(PipeMessageReceiver... parts);

    /** Sends a custom message from a pluggable or pipe centre to the server/client (depending on which side this is
     * currently on). */
    void sendMessage(PipeMessageReceiver to, IWriter writer);

    void sendGuiMessage(PipeMessageReceiver to, IWriter writer);

    /** Called on the server whenever a gui container object is opened. */
    void onPlayerOpen(EntityPlayer player);

    /** Called on the server whenever a gui container object is closed. */
    void onPlayerClose(EntityPlayer player);

    enum PipeMessageReceiver {
        BEHAVIOUR(null),
        FLOW(null),
        PLUGGABLE_DOWN(EnumFacing.DOWN),
        PLUGGABLE_UP(EnumFacing.UP),
        PLUGGABLE_NORTH(EnumFacing.NORTH),
        PLUGGABLE_SOUTH(EnumFacing.SOUTH),
        PLUGGABLE_WEST(EnumFacing.WEST),
        PLUGGABLE_EAST(EnumFacing.EAST),
        WIRES(null);
        // Wires are updated differently (they never use this API)

        public static final PipeMessageReceiver[] VALUES = values();
        public static final PipeMessageReceiver[] PLUGGABLES = new PipeMessageReceiver[6];

        static {
            for (PipeMessageReceiver type : VALUES) {
                if (type.face != null) {
                    PLUGGABLES[type.face.ordinal()] = type;
                }
            }
        }

        public final EnumFacing face;

        PipeMessageReceiver(EnumFacing face) {
            this.face = face;
        }
    }

    interface IWriter {
        void write(PacketBuffer buffer);
    }
}
