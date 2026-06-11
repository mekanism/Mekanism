package mekanism.common.network.to_server;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import mekanism.api.functions.TriConsumer;
import mekanism.api.security.IEntitySecurityUtils;
import mekanism.common.Mekanism;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.entity.robit.MainRobitContainer;
import mekanism.common.lib.security.SecurityUtils;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/// Used for informing the server that an action happened in an entity GUI
public record PacketEntityGuiInteract(GuiInteractionEntity entityInteraction, int entityID, int extra) implements IMekanismPacket {

    public static final Type<PacketEntityGuiInteract> TYPE = new Type<>(Mekanism.rl("entity_gui_interact"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketEntityGuiInteract> STREAM_CODEC = StreamCodec.composite(
          GuiInteractionEntity.STREAM_CODEC, packet -> packet.entityInteraction,
          ByteBufCodecs.VAR_INT, packet -> packet.entityID,
          ByteBufCodecs.VAR_INT, packet -> packet.extra,
          PacketEntityGuiInteract::new
    );

    public PacketEntityGuiInteract(GuiInteractionEntity interaction, Entity entity) {
        this(interaction, entity, 0);
    }

    public PacketEntityGuiInteract(GuiInteractionEntity interaction, Entity entity, int extra) {
        this(interaction, entity.getId(), extra);
    }

    @Override
    public Type<PacketEntityGuiInteract> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        Entity entity = player.level().getEntity(entityID);
        if (entity != null) {
            entityInteraction.consume(entity, player, extra);
        }
    }

    public enum GuiInteractionEntity {
        NEXT_SECURITY_MODE((entity, player, _) -> SecurityUtils.get().incrementSecurityMode(player, IEntitySecurityUtils.INSTANCE.securityCapability(entity), null)),
        PREVIOUS_SECURITY_MODE((entity, player, _) -> SecurityUtils.get().decrementSecurityMode(player, IEntitySecurityUtils.INSTANCE.securityCapability(entity), null)),
        CONTAINER_STOP_TRACKING((_, player, extra) -> {
            if (player.containerMenu instanceof MekanismContainer container) {
                container.stopTracking(extra);
            }
        }),
        CONTAINER_TRACK_SKIN_SELECT((_, player, extra) -> {
            if (player.containerMenu instanceof MainRobitContainer container) {
                container.startTrackingServer(extra, container);
            }
        }),

        GO_HOME((entity, player, _) -> {
            if (entity instanceof EntityRobit robit && IEntitySecurityUtils.INSTANCE.canAccess(player, robit)) {
                robit.goHome();
            }
        }),
        FOLLOW((entity, player, _) -> {
            if (entity instanceof EntityRobit robit && IEntitySecurityUtils.INSTANCE.canAccess(player, robit)) {
                robit.setFollowing(!robit.getFollowing());
            }
        }),
        PICKUP_DROPS((entity, player, _) -> {
            if (entity instanceof EntityRobit robit && IEntitySecurityUtils.INSTANCE.canAccess(player, robit)) {
                robit.setDropPickup(!robit.getDropPickup());
            }
        }),
        ;

        public static final IntFunction<GuiInteractionEntity> BY_ID = ByIdMap.continuous(GuiInteractionEntity::ordinal, values(), OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, GuiInteractionEntity> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, GuiInteractionEntity::ordinal);

        private final TriConsumer<Entity, Player, Integer> consumerForEntity;

        GuiInteractionEntity(TriConsumer<Entity, Player, Integer> consumerForEntity) {
            this.consumerForEntity = consumerForEntity;
        }

        public void consume(Entity entity, Player player, int extra) {
            consumerForEntity.accept(entity, player, extra);
        }
    }
}