package mekanism.common.network;

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

    private GuiType type;

    public PacketOpenGui(GuiType type) {
        this.type = type;
    }

    public static void handle(PacketOpenGui message, Supplier<Context> context) {
        PlayerEntity player = PacketHandler.getPlayer(context);
        if (player == null) {
            return;
        }
        context.get().enqueueWork(() -> NetworkHooks.openGui((ServerPlayerEntity) player, message.type.containerSupplier.get()));
        context.get().setPacketHandled(true);
    }

    public static void encode(PacketOpenGui pkt, PacketBuffer buf) {
        buf.writeInt(pkt.type.ordinal());
    }

    public static PacketOpenGui decode(PacketBuffer buf) {
        return new PacketOpenGui(GuiType.values()[buf.readInt()]);
    }

    public static enum GuiType {
        MODULE_TWEAKER(() -> new ContainerProvider(MekanismLang.MODULE_TWEAKER, (i, inv, p) -> new ModuleTweakerContainer(i, inv)));

        private Supplier<INamedContainerProvider> containerSupplier;

        private GuiType(Supplier<INamedContainerProvider> containerSupplier) {
            this.containerSupplier = containerSupplier;
        }
    }
}
