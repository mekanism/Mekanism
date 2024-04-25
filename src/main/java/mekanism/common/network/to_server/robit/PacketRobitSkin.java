package mekanism.common.network.to_server.robit;

import io.netty.buffer.ByteBuf;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.RobitSkin;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketRobitSkin(int entityId, ResourceKey<RobitSkin> skin) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketRobitSkin> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("robit_skin"));
    public static final StreamCodec<ByteBuf, PacketRobitSkin> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.VAR_INT, PacketRobitSkin::entityId,
          ResourceKey.streamCodec(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME), PacketRobitSkin::skin,
          PacketRobitSkin::new
    );

    public PacketRobitSkin(EntityRobit robit, @NotNull ResourceKey<RobitSkin> skin) {
        this(robit.getId(), skin);
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketRobitSkin> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        Entity entity = player.level().getEntity(entityId);
        if (entity instanceof EntityRobit robit) {//Note: setSkin will validate that the player can access the robit
            robit.setSkin(skin, player);
        }
    }
}