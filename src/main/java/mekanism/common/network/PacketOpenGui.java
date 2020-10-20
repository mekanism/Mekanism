package mekanism.common.network;

import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkHooks;

public class PacketOpenGui {

    private final GuiType type;

    public PacketOpenGui(GuiType type) {
        this.type = type;
    }

    public static void handle(PacketOpenGui message, Supplier<Context> context) {
        Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity player = ctx.getSender();
            if (player != null && message.type.shouldOpenForPlayer.test(player)) {
                NetworkHooks.openGui(player, message.type.containerSupplier.get());
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void encode(PacketOpenGui pkt, PacketBuffer buf) {
        buf.writeEnumValue(pkt.type);
    }

    public static PacketOpenGui decode(PacketBuffer buf) {
        return new PacketOpenGui(buf.readEnumValue(GuiType.class));
    }

    public enum GuiType {
        MODULE_TWEAKER(() -> new ContainerProvider(MekanismLang.MODULE_TWEAKER, (i, inv, p) -> new ModuleTweakerContainer(i, inv)), ModuleTweakerContainer::hasTweakableItem);

        private final Supplier<INamedContainerProvider> containerSupplier;
        private final Predicate<PlayerEntity> shouldOpenForPlayer;

        GuiType(Supplier<INamedContainerProvider> containerSupplier) {
            this(containerSupplier, player -> true);
        }

        GuiType(Supplier<INamedContainerProvider> containerSupplier, Predicate<PlayerEntity> shouldOpenForPlayer) {
            this.containerSupplier = containerSupplier;
            this.shouldOpenForPlayer = shouldOpenForPlayer;
        }
    }
}
