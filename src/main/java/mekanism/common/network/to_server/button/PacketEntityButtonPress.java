package mekanism.common.network.to_server.button;

import io.netty.buffer.ByteBuf;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used for informing the server that a click happened in a GUI and the gui window needs to change
 */
public record PacketEntityButtonPress(ClickedEntityButton buttonClicked, int entityID) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketEntityButtonPress> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("entity_button"));
    public static final StreamCodec<ByteBuf, PacketEntityButtonPress> STREAM_CODEC = StreamCodec.composite(
          ClickedEntityButton.STREAM_CODEC, PacketEntityButtonPress::buttonClicked,
          ByteBufCodecs.VAR_INT, PacketEntityButtonPress::entityID,
          PacketEntityButtonPress::new
    );

    public PacketEntityButtonPress(ClickedEntityButton buttonClicked, Entity entity) {
        this(buttonClicked, entity.getId());
    }

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketEntityButtonPress> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        Entity entity = player.level().getEntity(entityID);
        MenuProvider provider = buttonClicked.getProvider(entity);
        if (provider != null) {
            player.openMenu(provider, buf -> {
                buf.writeVarInt(entityID);
                buttonClicked.encodeExtraData(buf, entity);
            });
        }
    }

    public enum ClickedEntityButton {
        ROBIT_CRAFTING(entity -> MekanismContainerTypes.CRAFTING_ROBIT.getProvider(MekanismLang.ROBIT_CRAFTING, entity)),
        ROBIT_INVENTORY(entity -> MekanismContainerTypes.INVENTORY_ROBIT.getProvider(MekanismLang.ROBIT_INVENTORY, entity)),
        ROBIT_MAIN(entity -> MekanismContainerTypes.MAIN_ROBIT.getProvider(MekanismLang.ROBIT, entity)),
        ROBIT_REPAIR(entity -> MekanismContainerTypes.REPAIR_ROBIT.getProvider(MekanismLang.ROBIT_REPAIR, entity)),
        ROBIT_SMELTING(entity -> MekanismContainerTypes.SMELTING_ROBIT.getProvider(MekanismLang.ROBIT_SMELTING, entity));

        public static final IntFunction<ClickedEntityButton> BY_ID = ByIdMap.continuous(ClickedEntityButton::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, ClickedEntityButton> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ClickedEntityButton::ordinal);

        private final Function<Entity, @Nullable MenuProvider> providerFromEntity;
        @Nullable
        private final BiConsumer<RegistryFriendlyByteBuf, Entity> extraEncodingData;

        ClickedEntityButton(Function<Entity, @Nullable MenuProvider> providerFromEntity) {
            this(providerFromEntity, null);
        }

        ClickedEntityButton(Function<Entity, @Nullable MenuProvider> providerFromEntity, @Nullable BiConsumer<RegistryFriendlyByteBuf, Entity> extraEncodingData) {
            this.providerFromEntity = providerFromEntity;
            this.extraEncodingData = extraEncodingData;
        }

        @Nullable
        @Contract("null -> null")
        public MenuProvider getProvider(@Nullable Entity entity) {
            return entity == null ? null : providerFromEntity.apply(entity);
        }

        private void encodeExtraData(RegistryFriendlyByteBuf buffer, Entity entity) {
            if (extraEncodingData != null) {
                extraEncodingData.accept(buffer, entity);
            }
        }
    }
}