package mekanism.common.network.to_server.button;

import java.util.function.Function;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used for informing the server that a click happened in a GUI and the gui window needs to change
 */
public record PacketEntityButtonPress(ClickedEntityButton buttonClicked, int entityID) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("entity_button");

    public PacketEntityButtonPress(FriendlyByteBuf buffer) {
        this(buffer.readEnum(ClickedEntityButton.class), buffer.readVarInt());
    }

    public PacketEntityButtonPress(ClickedEntityButton buttonClicked, Entity entity) {
        this(buttonClicked, entity.getId());
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
            Entity entity = player.level().getEntity(entityID);
            if (entity != null) {
                player.openMenu(buttonClicked.getProvider(entity), buf -> buf.writeVarInt(entityID));
            }
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeEnum(buttonClicked);
        buffer.writeVarInt(entityID);
    }

    public enum ClickedEntityButton {
        ROBIT_CRAFTING(entity -> MekanismContainerTypes.CRAFTING_ROBIT.getProvider(MekanismLang.ROBIT_CRAFTING, entity)),
        ROBIT_INVENTORY(entity -> MekanismContainerTypes.INVENTORY_ROBIT.getProvider(MekanismLang.ROBIT_INVENTORY, entity)),
        ROBIT_MAIN(entity -> MekanismContainerTypes.MAIN_ROBIT.getProvider(MekanismLang.ROBIT, entity)),
        ROBIT_REPAIR(entity -> MekanismContainerTypes.REPAIR_ROBIT.getProvider(MekanismLang.ROBIT_REPAIR, entity)),
        ROBIT_SMELTING(entity -> MekanismContainerTypes.SMELTING_ROBIT.getProvider(MekanismLang.ROBIT_SMELTING, entity));

        private final Function<Entity, @Nullable MenuProvider> providerFromEntity;

        ClickedEntityButton(Function<Entity, @Nullable MenuProvider> providerFromEntity) {
            this.providerFromEntity = providerFromEntity;
        }

        @Nullable
        public MenuProvider getProvider(Entity entity) {
            return providerFromEntity.apply(entity);
        }
    }
}