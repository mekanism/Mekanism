package mekanism.common.network;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.filter.FilterManager;
import mekanism.common.lib.math.Range3D;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.TileComponentEjector;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.tile.interfaces.ITileFilterHolder;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

public class PacketUtils {

    private PacketUtils() {
    }

    //Note: This might be longer as if the name can't be found we have two characters plus an uuid (36)
    public static final int LAST_USERNAME_LENGTH = Math.max(Player.MAX_NAME_LENGTH, 38);

    private static final PacketDistributor<DynamicBufferedNetwork<?, ?, ?, ?>> TRACKING_NETWORK = new PacketDistributor<>(PacketUtils::trackingNetwork, PacketFlow.CLIENTBOUND);

    public static void log(String logFormat, Object... params) {
        //TODO: Add more logging for packets using this
        if (MekanismConfig.general.logPackets.get()) {
            Mekanism.logger.info(logFormat, params);
        }
    }

    //TODO - 1.20.4: SP: Re-evaluate use cases of this and if there is a better way to handle them
    public static <OBJ> OBJ read(byte[] rawData, FriendlyByteBuf.Reader<OBJ> deserializer) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(rawData));
        try {
            return deserializer.apply(buffer);
        } finally {
            buffer.release();
        }
    }

    public static <KEY, V1, V2> void writeMultipleMaps(FriendlyByteBuf buffer, Map<KEY, V1> map1, Map<KEY, V2> map2, FriendlyByteBuf.Writer<KEY> keyWriter,
          FriendlyByteBuf.Writer<V1> v1Writer, FriendlyByteBuf.Writer<V2> v2Writer) {
        if (map1.size() != map2.size()) {
            throw new IllegalArgumentException("Expected map1 and map2 to have the same size");
        }
        buffer.writeVarInt(map1.size());
        for (Map.Entry<KEY, V1> entry : map1.entrySet()) {
            KEY key = entry.getKey();
            keyWriter.accept(buffer, key);
            v1Writer.accept(buffer, entry.getValue());
            V2 v2 = map2.get(key);
            if (v2 == null) {
                throw new IllegalArgumentException("Expected maps to have the same keys but map2 was missing key " + key);
            }
            v2Writer.accept(buffer, v2);
        }
    }

    public static <KEY, V1, V2> Pair<Map<KEY, V1>, Map<KEY, V2>> readMultipleMaps(FriendlyByteBuf buffer, FriendlyByteBuf.Reader<KEY> keyReader, FriendlyByteBuf.Reader<V1> v1Reader, FriendlyByteBuf.Reader<V2> v2Reader) {
        return readMultipleMaps(buffer, Maps::newHashMapWithExpectedSize, Maps::newHashMapWithExpectedSize, keyReader, v1Reader, v2Reader);
    }

    public static <KEY, V1, V2, M1 extends Map<KEY, V1>, M2 extends Map<KEY, V2>> Pair<M1, M2> readMultipleMaps(FriendlyByteBuf buffer, IntFunction<M1> map1Factory, IntFunction<M2> map2Factory,
          FriendlyByteBuf.Reader<KEY> keyReader, FriendlyByteBuf.Reader<V1> v1Reader, FriendlyByteBuf.Reader<V2> v2Reader) {
        int size = buffer.readVarInt();
        M1 map1 = map1Factory.apply(size);
        M2 map2 = map2Factory.apply(size);
        for (int element = 0; element < size; element++) {
            KEY key = keyReader.apply(buffer);
            map1.put(key, v1Reader.apply(buffer));
            map2.put(key, v2Reader.apply(buffer));
        }
        return Pair.of(map1, map2);
    }

    public static Optional<ServerPlayer> asServerPlayer(IPayloadContext context) {
        return context.player()
              .filter(ServerPlayer.class::isInstance)
              .map(ServerPlayer.class::cast);
    }

    @Nullable
    public static TileComponentEjector ejector(IPayloadContext context, BlockPos pos) {
        ISideConfiguration sideConfig = blockEntity(context, pos, ISideConfiguration.class);
        return sideConfig == null ? null : sideConfig.getEjector();
    }

    @Nullable
    public static TileComponentConfig config(IPayloadContext context, BlockPos pos) {
        ISideConfiguration sideConfig = blockEntity(context, pos, ISideConfiguration.class);
        return sideConfig == null ? null : sideConfig.getConfig();
    }

    @Nullable
    public static FilterManager<?> filterManager(IPayloadContext context, BlockPos pos) {
        ITileFilterHolder<?> filterHolder = blockEntity(context, pos, ITileFilterHolder.class);
        return filterHolder == null ? null : filterHolder.getFilterManager();
    }

    @Nullable
    public static <CLASS> CLASS blockEntity(IPayloadContext context, BlockPos pos, Class<CLASS> clazz) {
        BlockEntity be = blockEntity(context, pos);
        if (clazz.isInstance(be)) {
            return clazz.cast(be);
        }
        return null;
    }

    @Nullable
    public static BlockEntity blockEntity(IPayloadContext context, BlockPos pos) {
        return WorldUtils.getTileEntity(context.level().orElse(null), pos);
    }

    public static <CLASS extends AbstractContainerMenu> Optional<CLASS> container(IPayloadContext context, Class<CLASS> clazz) {
        return context.player()
              .map(player -> player.containerMenu)
              .filter(clazz::isInstance)
              .map(clazz::cast);
    }

    /**
     * Send this message to the specified player.
     *
     * @param message - the message to send
     * @param player  - the player to send it to
     */
    public static <MSG extends CustomPacketPayload> void sendTo(MSG message, ServerPlayer player) {
        PacketDistributor.PLAYER.with(player).send(message);
    }

    /**
     * Send this message to everyone connected to the server.
     *
     * @param message - message to send
     */
    public static <MSG extends CustomPacketPayload> void sendToAll(MSG message) {
        PacketDistributor.ALL.noArg().send(message);
    }

    /**
     * Send this message to everyone connected to the server if the server has loaded.
     *
     * @param message - message to send
     *
     * @apiNote This is useful for reload listeners
     */
    public static <MSG extends CustomPacketPayload> void sendToAllIfLoaded(MSG message) {
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
    public static <MSG extends CustomPacketPayload> void sendToDimension(MSG message, ResourceKey<Level> dimension) {
        PacketDistributor.DIMENSION.with(dimension).send(message);
    }

    /**
     * Send this message to the server.
     *
     * @param message - the message to send
     */
    public static <MSG extends CustomPacketPayload> void sendToServer(MSG message) {
        PacketDistributor.SERVER.noArg().send(message);
    }

    public static <MSG extends CustomPacketPayload> void sendToAllTracking(MSG message, Entity entity) {
        PacketDistributor.TRACKING_ENTITY.with(entity).send(message);
    }

    public static <MSG extends CustomPacketPayload> void sendToAllTrackingAndSelf(MSG message, Entity entity) {
        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(entity).send(message);
    }

    public static <MSG extends CustomPacketPayload> void sendToAllTracking(MSG message, BlockEntity tile) {
        sendToAllTracking(message, tile.getLevel(), tile.getBlockPos());
    }

    public static <MSG extends CustomPacketPayload> void sendToAllTracking(MSG message, Level world, BlockPos pos) {
        if (world instanceof ServerLevel level) {
            //If we have a ServerWorld just directly figure out the ChunkPos to not require looking up the chunk
            // This provides a decent performance boost over using the packet distributor
            for (ServerPlayer p : level.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false)) {
                sendTo(message, p);
            }
        } else {
            //Otherwise, fallback to entities tracking the chunk if some mod did something odd and our world is not a ServerWorld
            PacketDistributor.TRACKING_CHUNK.with(world.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()))).send(message);
        }
    }

    //TODO: Evaluate moving various network related packets over to this (and making it support non buffered networks)
    public static void sendToAllTracking(DynamicBufferedNetwork<?, ?, ?, ?> network, CustomPacketPayload... packets) {
        TRACKING_NETWORK.with(network).send(packets);
    }

    private static boolean isChunkTracked(ServerPlayer player, int chunkX, int chunkZ) {
        return player.getChunkTrackingView().contains(chunkX, chunkZ) && !player.connection.chunkSender.isPending(ChunkPos.asLong(chunkX, chunkZ));
    }

    private static <NETWORK extends DynamicBufferedNetwork<?, ?, ?, ?>> Consumer<Packet<?>> trackingNetwork(PacketDistributor<NETWORK> packetDistributor, NETWORK network) {
        return p -> {
            Range3D range = network.getPacketRange();
            //TODO: Create a method in DynamicNetwork to get all players that are "tracking" the network
            // Also evaluate moving various network packet things over to using this at that point
            //TODO - 1.20.4: If we just make the packet range instead keep track of the ChunkPositions that then we check if the player is tracking
            // it will allow us to have it hopefully work a bit more accurately and more cleanly?
            // and that way we can do a check that the player is tracking one of the chunks maybe instead of doing the weird radius check
            PlayerList playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList();
            //Ignore height for partial Cubic chunks support as range comparison gets used ignoring player height normally anyway
            int radius = playerList.getViewDistance() * 16;
            for (ServerPlayer player : playerList.getPlayers()) {
                if (range.dimension() == player.level().dimension()) {
                    //TODO - 1.20.4: Should this use proper position instead of the block position (if we switch to tracked chunks then this becomes irrelevant)
                    BlockPos playerPosition = player.blockPosition();
                    int playerX = playerPosition.getX();
                    int playerZ = playerPosition.getZ();
                    //playerX/Z + radius is the max, so to stay in line with how it was before, it has an extra + 1 added to it
                    if (playerX + radius + 1.99999 > range.xMin() && range.xMax() + 0.99999 > playerX - radius &&
                        playerZ + radius + 1.99999 > range.zMin() && range.zMax() + 0.99999 > playerZ - radius) {
                        player.connection.send(p);
                    }
                }
            }
        };
    }
}