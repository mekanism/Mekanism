package mekanism.common.network.to_server;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketOpenGui(GuiType guiType) implements IMekanismPacket {

    public static final CustomPacketPayload.Type<PacketOpenGui> TYPE = new CustomPacketPayload.Type<>(Mekanism.rl("open_gui"));
    public static final StreamCodec<ByteBuf, PacketOpenGui> STREAM_CODEC = GuiType.STREAM_CODEC.map(
          PacketOpenGui::new, PacketOpenGui::guiType
    );

    @NotNull
    @Override
    public CustomPacketPayload.Type<PacketOpenGui> type() {
        return TYPE;
    }

    @Override
    public void handle(IPayloadContext context) {
        Player player = context.player();
        if (guiType.shouldOpenForPlayer.test(player)) {
            player.openMenu(guiType.containerSupplier.get());
        }
    }

    public enum GuiType {
        MODULE_TWEAKER(() -> new ContainerProvider(MekanismLang.MODULE_TWEAKER, (id, inv, player) -> MekanismContainerTypes.MODULE_TWEAKER.get().create(id, inv)),
              ModuleTweakerContainer::hasTweakableItem);

        public static final IntFunction<GuiType> BY_ID = ByIdMap.continuous(GuiType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, GuiType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, GuiType::ordinal);

        private final Supplier<MenuProvider> containerSupplier;
        private final Predicate<Player> shouldOpenForPlayer;

        GuiType(Supplier<MenuProvider> containerSupplier) {
            this(containerSupplier, ConstantPredicates.alwaysTrue());
        }

        GuiType(Supplier<MenuProvider> containerSupplier, Predicate<Player> shouldOpenForPlayer) {
            this.containerSupplier = containerSupplier;
            this.shouldOpenForPlayer = shouldOpenForPlayer;
        }
    }
}
