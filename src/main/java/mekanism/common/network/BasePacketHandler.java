package mekanism.common.network;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.Range3D;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public abstract class BasePacketHandler {

    protected static SimpleChannel createChannel(ResourceLocation name) {
        return NetworkRegistry.ChannelBuilder.named(name)
              .clientAcceptedVersions(getProtocolVersion()::equals)
              .serverAcceptedVersions(getProtocolVersion()::equals)
              .networkProtocolVersion(BasePacketHandler::getProtocolVersion)
              .simpleChannel();
    }

    private static String getProtocolVersion() {
        return Mekanism.instance == null ? "999.999.999" : Mekanism.instance.versionNumber.toString();
    }

    /**
     * Helper for reading strings to make sure we don't accidentally call PacketBuffer#readString on the server
     */
    public static String readString(PacketBuffer buffer) {
        //TODO: Evaluate usages and potentially move some things to more strict string length checks
        return buffer.readString(Short.MAX_VALUE);
    }

    public static Vector3d readVector3d(PacketBuffer buffer) {
        return new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void writeVector3d(PacketBuffer buffer, Vector3d vector) {
        buffer.writeDouble(vector.getX());
        buffer.writeDouble(vector.getY());
        buffer.writeDouble(vector.getZ());
    }

    public static void log(String log) {
        //TODO: Add more logging for packets using this
        if (MekanismConfig.general.logPackets.get()) {
            Mekanism.logger.info(log);
        }
    }

    private int index = 0;

    protected abstract SimpleChannel getChannel();

    public abstract void initialize();

    protected <MSG> void registerClientToServer(Class<MSG> type, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder,
          BiConsumer<MSG, Supplier<Context>> consumer) {
        getChannel().registerMessage(index++, type, encoder, decoder, consumer, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    protected <MSG> void registerServerToClient(Class<MSG> type, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder,
          BiConsumer<MSG, Supplier<Context>> consumer) {
        getChannel().registerMessage(index++, type, encoder, decoder, consumer, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    /**
     * Send this message to the specified player.
     *
     * @param message - the message to send
     * @param player  - the player to send it to
     */
    public <MSG> void sendTo(MSG message, ServerPlayerEntity player) {
        getChannel().sendTo(message, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }

    /**
     * Send this message to everyone connected to the server.
     *
     * @param message - message to send
     */
    public <MSG> void sendToAll(MSG message) {
        getChannel().send(PacketDistributor.ALL.noArg(), message);
    }

    /**
     * Send this message to everyone connected to the server if the server has loaded.
     *
     * @param message - message to send
     *
     * @apiNote This is useful for reload listeners
     */
    public <MSG> void sendToAllIfLoaded(MSG message) {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            //If the server has loaded, send to all players
            sendToAll(message);
        }
    }

    /**
     * Send this message to everyone within the supplied dimension.
     *
     * @param message   - the message to send
     * @param dimension - the dimension to target
     */
    public <MSG> void sendToDimension(MSG message, RegistryKey<World> dimension) {
        getChannel().send(PacketDistributor.DIMENSION.with(() -> dimension), message);
    }

    /**
     * Send this message to the server.
     *
     * @param message - the message to send
     */
    public <MSG> void sendToServer(MSG message) {
        getChannel().sendToServer(message);
    }

    public <MSG> void sendToAllTracking(MSG message, Entity entity) {
        getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
    }

    public <MSG> void sendToAllTrackingAndSelf(MSG message, Entity entity) {
        getChannel().send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }

    public <MSG> void sendToAllTracking(MSG message, TileEntity tile) {
        sendToAllTracking(message, tile.getWorld(), tile.getPos());
    }

    public <MSG> void sendToAllTracking(MSG message, World world, BlockPos pos) {
        if (world instanceof ServerWorld) {
            //If we have a ServerWorld just directly figure out the ChunkPos so as to not require looking up the chunk
            // This provides a decent performance boost over using the packet distributor
            ((ServerWorld) world).getChunkProvider().chunkManager.getTrackingPlayers(new ChunkPos(pos), false).forEach(p -> sendTo(message, p));
        } else {
            //Otherwise fallback to entities tracking the chunk if some mod did something odd and our world is not a ServerWorld
            getChannel().send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunk(pos.getX() >> 4, pos.getZ() >> 4)), message);
        }
    }

    public <MSG> void sendToReceivers(MSG message, DynamicBufferedNetwork<?, ?, ?, ?> network) {
        //TODO: Figure out why we have a try catch and remove the need for it
        try {
            //TODO: Create a method in DynamicNetwork to get all players that are "tracking" the network
            // Also evaluate moving various network packet things over to using this at that point
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                Range3D range = network.getPacketRange();
                PlayerList playerList = server.getPlayerList();
                //Ignore height for partial Cubic chunks support as range comparision gets used ignoring player height normally anyways
                int radius = playerList.getViewDistance() * 16;
                for (ServerPlayerEntity player : playerList.getPlayers()) {
                    if (range.dimension == player.func_241141_L_()) {
                        BlockPos playerPosition = player.getPosition();
                        int playerX = playerPosition.getX();
                        int playerZ = playerPosition.getZ();
                        //playerX/Z + radius is the max, so to stay in line with how it was before, it has an extra + 1 added to it
                        if (playerX + radius + 1.99999 > range.xMin && range.xMax + 0.99999 > playerX - radius &&
                            playerZ + radius + 1.99999 > range.zMin && range.zMax + 0.99999 > playerZ - radius) {
                            sendTo(message, player);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}