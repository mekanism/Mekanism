package mekanism.common.network.to_server;

import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

public class PacketOpenGui implements IMekanismPacket {

    private final GuiType type;

    public PacketOpenGui(GuiType type) {
        this.type = type;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null && type.shouldOpenForPlayer.test(player)) {
            NetworkHooks.openScreen(player, type.containerSupplier.get());
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(type);
    }

    public static PacketOpenGui decode(FriendlyByteBuf buffer) {
        return new PacketOpenGui(buffer.readEnum(GuiType.class));
    }

    public enum GuiType {
        MODULE_TWEAKER(() -> new ContainerProvider(MekanismLang.MODULE_TWEAKER, (id, inv, player) -> MekanismContainerTypes.MODULE_TWEAKER.get().create(id, inv)),
              ModuleTweakerContainer::hasTweakableItem);

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
