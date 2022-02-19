package mekanism.common.network;

import java.util.Optional;
import java.util.function.Function;
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
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkDirection;
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
     * Helper for reading strings to make sure we don't accidentally call {@link PacketBuffer#readUtf()} on the server
     */
    public static String readString(PacketBuffer buffer) {
        //TODO - 1.18: Evaluate usages and potentially move some things to more strict string length checks
        return buffer.readUtf(Short.MAX_VALUE);
    }

    public static Vector3d readVector3d(PacketBuffer buffer) {
        return new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void writeVector3d(PacketBuffer buffer, Vector3d vector) {
        buffer.writeDouble(vector.x());
        buffer.writeDouble(vector.y());
        buffer.writeDouble(vector.z());
    }

    public static void log(String logFormat, Object... params) {
        //TODO: Add more logging for packets using this
        if (MekanismConfig.general.logPackets.get()) {
            Mekanism.logger.info(logFormat, params);
        }
    }

    private int index = 0;

    protected abstract SimpleChannel getChannel();

    public abstract void initialize();

    protected <MSG extends IMekanismPacket> void registerClientToServer(Class<MSG> type, Function<PacketBuffer, MSG> decoder) {
        registerMessage(type, decoder, NetworkDirection.PLAY_TO_SERVER);
    }

    protected <MSG extends IMekanismPacket> void registerServerToClient(Class<MSG> type, Function<PacketBuffer, MSG> decoder) {
        registerMessage(type, decoder, NetworkDirection.PLAY_TO_CLIENT);
    }

    private <MSG extends IMekanismPacket> void registerMessage(Class<MSG> type, Function<PacketBuffer, MSG> decoder, NetworkDirection networkDirection) {
        getChannel().registerMessage(index++, type, IMekanismPacket::encode, decoder, IMekanismPacket::handle, Optional.of(networkDirection));
    }

    /**
     * Send this message to the specified player.
     *
     * @param message - the message to send
     * @param player  - the player to send it to
     */
    public <MSG> void sendTo(MSG message, ServerPlayerEntity player) {
        //Validate it is not a fake player, even though none of our code should call this with a fake player
        if (!(player instanceof FakePlayer)) {
            getChannel().sendTo(message, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
        }
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
        sendToAllTracking(message, tile.getLevel(), tile.getBlockPos());
    }

    public <MSG> void sendToAllTracking(MSG message, World world, BlockPos pos) {
        if (world instanceof ServerWorld) {
            //If we have a ServerWorld just directly figure out the ChunkPos to not require looking up the chunk
            // This provides a decent performance boost over using the packet distributor
            ((ServerWorld) world).getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false).forEach(p -> sendTo(message, p));
        } else {
            //Otherwise, fallback to entities tracking the chunk if some mod did something odd and our world is not a ServerWorld
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
                //Ignore height for partial Cubic chunks support as range comparison gets used ignoring player height normally anyway
                int radius = playerList.getViewDistance() * 16;
                for (ServerPlayerEntity player : playerList.getPlayers()) {
                    if (range.dimension == player.getLevel().dimension()) {
                        BlockPos playerPosition = player.blockPosition();
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