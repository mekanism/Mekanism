package mekanism.common.network.to_server.robit;

import mekanism.api.MekanismAPI;
import mekanism.api.robit.RobitSkin;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketRobitSkin(int entityId, ResourceKey<RobitSkin> skin) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("robit_skin");

    public PacketRobitSkin(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readResourceKey(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME));
    }

    public PacketRobitSkin(EntityRobit robit, @NotNull ResourceKey<RobitSkin> skin) {
        this(robit.getId(), skin);
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        Player player = context.player().orElse(null);
        if (player != null) {
            Entity entity = player.level().getEntity(entityId);
            if (entity instanceof EntityRobit robit) {//Note: setSkin will validate that the player can access the robit
                robit.setSkin(skin, player);
            }
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeResourceKey(skin);
    }
}