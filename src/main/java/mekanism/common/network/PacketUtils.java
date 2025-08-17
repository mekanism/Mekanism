package mekanism.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Function;
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
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PacketUtils {

    private PacketUtils() {
    }

    //Note: This might be longer as if the name can't be found we have two characters plus an uuid (36)
    public static final int LAST_USERNAME_LENGTH = Math.max(SharedConstants.MAX_PLAYER_NAME_LENGTH, 38);

    public static final StreamCodec<FriendlyByteBuf, BlockHitResult> BLOCK_HIT_RESULT_STREAM_CODEC = StreamCodec.of(FriendlyByteBuf::writeBlockHitResult, FriendlyByteBuf::readBlockHitResult);
    public static final StreamCodec<ByteBuf, InteractionHand> INTERACTION_HAND_STREAM_CODEC = enumCodec(InteractionHand.class);
    public static final StreamCodec<ByteBuf, EquipmentSlot> EQUIPMENT_SLOT_STREAM_CODEC = enumCodec(EquipmentSlot.class);
    public static final StreamCodec<ByteBuf, Vec3> VEC3_STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.DOUBLE, Vec3::x,
          ByteBufCodecs.DOUBLE, Vec3::y,
          ByteBufCodecs.DOUBLE, Vec3::z,
          Vec3::new
    );
    public static final StreamCodec<ByteBuf, OptionalDouble> OPTIONAL_DOUBLE_STREAM_CODEC = new StreamCodec<>() {
        @NotNull
        @Override
        public OptionalDouble decode(@NotNull ByteBuf buffer) {
            return buffer.readBoolean() ? OptionalDouble.of(buffer.readDouble()) : OptionalDouble.empty();
        }

        @Override
        public void encode(@NotNull ByteBuf buffer, @NotNull OptionalDouble value) {
            if (value.isPresent()) {
                buffer.writeBoolean(true);
                buffer.writeDouble(value.getAsDouble());
            } else {
                buffer.writeBoolean(false);
            }
        }
    };

    //Similar to NeoForgeStreamCodecs#enumCodec but allows for keeping it as a ByteBuf and wrapping the value
    public static <V extends Enum<V>> StreamCodec<ByteBuf, V> enumCodec(Class<V> enumClass) {
        return ByteBufCodecs.idMapper(ByIdMap.continuous(Enum::ordinal, enumClass.getEnumConstants(), ByIdMap.OutOfBoundsStrategy.WRAP), Enum::ordinal);
    }

    public static void log(String logFormat, Object... params) {
        //TODO: Add more logging for packets using this
        if (MekanismConfig.general.logPackets.get()) {
            Mekanism.logger.info(logFormat, params);
        }
    }

    public static <OBJ> OBJ read(RegistryAccess registryAccess, byte[] rawData, Function<RegistryFriendlyByteBuf, OBJ> deserializer) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(rawData), registryAccess);
        try {
            return deserializer.apply(buffer);
        } finally {
            buffer.release();
        }
    }

    @Nullable
    public static TileComponentEjector ejector(IPayloadContext context, BlockPos pos) {
        if (blockEntity(context, pos) instanceof ISideConfiguration sideConfig) {
            return sideConfig.getEjector();
        }
        return null;
    }

    @Nullable
    public static TileComponentConfig config(IPayloadContext context, BlockPos pos) {
        if (blockEntity(context, pos) instanceof ISideConfiguration sideConfig) {
            return sideConfig.getConfig();
        }
        return null;
    }

    @Nullable
    public static FilterManager<?> filterManager(IPayloadContext context, BlockPos pos) {
        if (blockEntity(context, pos) instanceof ITileFilterHolder<?> filterHolder) {
            return filterHolder.getFilterManager();
        }
        return null;
    }

    @Nullable
    public static BlockEntity blockEntity(IPayloadContext context, BlockPos pos) {
        return WorldUtils.getTileEntity(context.player().level(), pos);
    }

    @Nullable
    public static BlockEntity blockEntity(IPayloadContext context, long pos) {
        return WorldUtils.getTileEntity(context.player().level(), pos);
    }

    /**
     * Send this message to the server.
     *
     * @param message - the message to send
     */
    public static <MSG extends CustomPacketPayload> boolean sendToServer(MSG message) {
        PacketDistributor.sendToServer(message);
        return true;
    }

    public static <MSG extends CustomPacketPayload> void sendToAllTracking(MSG message, BlockEntity tile) {
        sendToAllTracking(message, tile.getLevel(), tile.getBlockPos());
    }

    public static <MSG extends CustomPacketPayload> void sendToAllTracking(MSG message, Level world, BlockPos pos) {
        PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) world, new ChunkPos(pos), message);
    }

    /**
     * Based on home {@link PacketDistributor#sendToPlayersTrackingChunk(ServerLevel, ChunkPos, CustomPacketPayload, CustomPacketPayload...)} finds players to send to,
     * without the immutable list.
     */
    public static boolean hasPlayersTracking(ServerLevel level, BlockPos pos) {
        int chunkX = SectionPos.blockToSectionCoord(pos.getX());
        int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
        Set<ServerPlayer> players = level.getChunkSource().chunkMap.playerMap.getAllPlayers();

        for (ServerPlayer serverplayer : players) {
            if (level.getChunkSource().chunkMap.isChunkTracked(serverplayer, chunkX, chunkZ)) {
                return true;
            }
        }
        return false;
    }

    //TODO: Evaluate moving various network related packets over to this (and making it support non buffered networks)
    public static void sendToAllTracking(DynamicBufferedNetwork<?, ?, ?, ?> network, CustomPacketPayload... packets) {
        Range3D range = network.getPacketRange();
        //TODO: Create a method in DynamicNetwork to get all players that are "tracking" the network
        // Also evaluate moving various network packet things over to using this at that point
        //TODO - 1.20.4: If we just make the packet range instead keep track of the ChunkPositions that then we check if the player is tracking
        // it will allow us to have it hopefully work a bit more accurately and more cleanly?
        // and that way we can do a check that the player is tracking one of the chunks maybe instead of doing the weird radius check
        PlayerList playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList();
        Packet<?> packet = makeClientboundPacket(packets);
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
                    player.connection.send(packet);
                }
            }
        }
    }

    private static Packet<?> makeClientboundPacket(CustomPacketPayload... payloads) {
        if (payloads.length > 1) {
            List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>(payloads.length);
            for (CustomPacketPayload otherPayload : payloads) {
                packets.add(new ClientboundCustomPayloadPacket(otherPayload));
            }
            return new ClientboundBundlePacket(packets);
        }
        return new ClientboundCustomPayloadPacket(payloads[0]);
    }
}