package mekanism.common.network;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import mekanism.api.functions.TriConsumer;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.Version;
import mekanism.common.lib.math.Range3D;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

public abstract class BasePacketHandler {

    protected static SimpleChannel createChannel(ResourceLocation name, Version version) {
        String protocolVersion = version.toString();
        return NetworkRegistry.ChannelBuilder.named(name)
              .clientAcceptedVersions(protocolVersion::equals)
              .serverAcceptedVersions(protocolVersion::equals)
              .networkProtocolVersion(() -> protocolVersion)
              .simpleChannel();
    }

    /**
     * Helper for reading strings to make sure we don't accidentally call {@link FriendlyByteBuf#readUtf()} on the server
     */
    public static String readString(FriendlyByteBuf buffer) {
        //TODO - 1.18: Evaluate usages and potentially move some things to more strict string length checks
        return buffer.readUtf(Short.MAX_VALUE);
    }

    public static Vec3 readVector3d(FriendlyByteBuf buffer) {
        return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void writeVector3d(FriendlyByteBuf buffer, Vec3 vector) {
        buffer.writeDouble(vector.x());
        buffer.writeDouble(vector.y());
        buffer.writeDouble(vector.z());
    }

    //Like FriendlyByteBuf#writeOptional but with nullable things instead
    public static <TYPE> void writeOptional(FriendlyByteBuf buffer, @Nullable TYPE value, BiConsumer<FriendlyByteBuf, TYPE> writer) {
        if (value == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            writer.accept(buffer, value);
        }
    }

    //Like FriendlyByteBuf#writeOptional but with nullable things instead
    @Nullable
    public static <TYPE> TYPE readOptional(FriendlyByteBuf buffer, Function<FriendlyByteBuf, TYPE> reader) {
        return buffer.readBoolean() ? reader.apply(buffer) : null;
    }

    public static <TYPE> void writeArray(FriendlyByteBuf buffer, TYPE[] array, BiConsumer<TYPE, FriendlyByteBuf> writer) {
        buffer.writeVarInt(array.length);
        for (TYPE element : array) {
            writer.accept(element, buffer);
        }
    }

    public static <TYPE> TYPE[] readArray(FriendlyByteBuf buffer, IntFunction<TYPE[]> arrayFactory, Function<FriendlyByteBuf, TYPE> reader) {
        TYPE[] array = arrayFactory.apply(buffer.readVarInt());
        for (int element = 0; element < array.length; element++) {
            array[element] = reader.apply(buffer);
        }
        return array;
    }

    public static <KEY, VALUE> void writeMap(FriendlyByteBuf buffer, Map<KEY, VALUE> map, TriConsumer<KEY, VALUE, FriendlyByteBuf> writer) {
        buffer.writeVarInt(map.size());
        map.forEach((key, value) -> writer.accept(key, value, buffer));
    }

    public static <KEY, VALUE, MAP extends Map<KEY, VALUE>> MAP readMap(FriendlyByteBuf buffer, IntFunction<MAP> mapFactory, Function<FriendlyByteBuf, KEY> keyReader,
          Function<FriendlyByteBuf, VALUE> valueReader) {
        int elements = buffer.readVarInt();
        MAP map = mapFactory.apply(elements);
        for (int element = 0; element < elements; element++) {
            map.put(keyReader.apply(buffer), valueReader.apply(buffer));
        }
        return map;
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

    protected <MSG extends IMekanismPacket> void registerClientToServer(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder) {
        registerMessage(type, decoder, NetworkDirection.PLAY_TO_SERVER);
    }

    protected <MSG extends IMekanismPacket> void registerServerToClient(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder) {
        registerMessage(type, decoder, NetworkDirection.PLAY_TO_CLIENT);
    }

    private <MSG extends IMekanismPacket> void registerMessage(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder, NetworkDirection networkDirection) {
        getChannel().registerMessage(index++, type, IMekanismPacket::encode, decoder, IMekanismPacket::handle, Optional.of(networkDirection));
    }

    /**
     * Send this message to the specified player.
     *
     * @param message - the message to send
     * @param player  - the player to send it to
     */
    public <MSG> void sendTo(MSG message, ServerPlayer player) {
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
    public <MSG> void sendToDimension(MSG message, ResourceKey<Level> dimension) {
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

    public <MSG> void sendToAllTracking(MSG message, BlockEntity tile) {
        sendToAllTracking(message, tile.getLevel(), tile.getBlockPos());
    }

    public <MSG> void sendToAllTracking(MSG message, Level world, BlockPos pos) {
        if (world instanceof ServerLevel level) {
            //If we have a ServerWorld just directly figure out the ChunkPos to not require looking up the chunk
            // This provides a decent performance boost over using the packet distributor
            level.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false).forEach(p -> sendTo(message, p));
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
                for (ServerPlayer player : playerList.getPlayers()) {
                    if (range.dimension() == player.getLevel().dimension()) {
                        BlockPos playerPosition = player.blockPosition();
                        int playerX = playerPosition.getX();
                        int playerZ = playerPosition.getZ();
                        //playerX/Z + radius is the max, so to stay in line with how it was before, it has an extra + 1 added to it
                        if (playerX + radius + 1.99999 > range.xMin() && range.xMax() + 0.99999 > playerX - radius &&
                            playerZ + radius + 1.99999 > range.zMin() && range.zMax() + 0.99999 > playerZ - radius) {
                            sendTo(message, player);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}