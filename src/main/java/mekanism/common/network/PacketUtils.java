package mekanism.common.network;

import com.mojang.datafixers.util.Function9;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
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
    //TODO - 1.20.5: Do we want this ot be FriendlyByteBuf or do like vanilla does and make an id mapping and let it just be a ByteBuf?
    public static final StreamCodec<FriendlyByteBuf, InteractionHand> INTERACTION_HAND_STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(InteractionHand.class);
    public static final StreamCodec<FriendlyByteBuf, EquipmentSlot> EQUIPMENT_SLOT_STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(EquipmentSlot.class);
    public static final StreamCodec<ByteBuf, Vec3> VEC3_STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.DOUBLE, Vec3::x,
          ByteBufCodecs.DOUBLE, Vec3::y,
          ByteBufCodecs.DOUBLE, Vec3::z,
          Vec3::new
    );

    public static void log(String logFormat, Object... params) {
        //TODO: Add more logging for packets using this
        if (MekanismConfig.general.logPackets.get()) {
            Mekanism.logger.info(logFormat, params);
        }
    }

    //TODO - 1.20.5: I believe we can once again move away from having to convert things to raw as full serialization happens in vanilla even in single player?
    //TODO - 1.20.4: SP: Re-evaluate use cases of this and if there is a better way to handle them
    public static <OBJ> OBJ read(RegistryAccess registryAccess, byte[] rawData, Function<RegistryFriendlyByteBuf, OBJ> deserializer) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(rawData), registryAccess);
        try {
            return deserializer.apply(buffer);
        } finally {
            buffer.release();
        }
    }

    public static <T> StreamCodec<ByteBuf, TagKey<T>> tagKeyCodec(ResourceKey<? extends Registry<T>> registry) {
        return ResourceLocation.STREAM_CODEC.map(rl -> TagKey.create(registry, rl), TagKey::location);
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

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9> StreamCodec<B, C> composite(
          final StreamCodec<? super B, T1> codec1, final Function<C, T1> getter1,
          final StreamCodec<? super B, T2> codec2, final Function<C, T2> getter2,
          final StreamCodec<? super B, T3> codec3, final Function<C, T3> getter3,
          final StreamCodec<? super B, T4> codec4, final Function<C, T4> getter4,
          final StreamCodec<? super B, T5> codec5, final Function<C, T5> getter5,
          final StreamCodec<? super B, T6> codec6, final Function<C, T6> getter6,
          final StreamCodec<? super B, T7> codec7, final Function<C, T7> getter7,
          final StreamCodec<? super B, T8> codec8, final Function<C, T8> getter8,
          final StreamCodec<? super B, T9> codec9, final Function<C, T9> getter9,
          final Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, C> factory) {
        return new StreamCodec<>() {
            @NotNull
            @Override
            public C decode(@NotNull B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
            }

            @Override
            public void encode(@NotNull B buffer, @NotNull C obj) {
                codec1.encode(buffer, getter1.apply(obj));
                codec2.encode(buffer, getter2.apply(obj));
                codec3.encode(buffer, getter3.apply(obj));
                codec4.encode(buffer, getter4.apply(obj));
                codec5.encode(buffer, getter5.apply(obj));
                codec6.encode(buffer, getter6.apply(obj));
                codec7.encode(buffer, getter7.apply(obj));
                codec8.encode(buffer, getter8.apply(obj));
                codec9.encode(buffer, getter9.apply(obj));
            }
        };
    }
}