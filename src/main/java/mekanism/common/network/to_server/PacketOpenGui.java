package mekanism.common.network.to_server;

import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.ModuleTweakerContainer;
import mekanism.common.network.IMekanismPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;

public class PacketOpenGui implements IMekanismPacket {

    private final GuiType type;

    public PacketOpenGui(GuiType type) {
        this.type = type;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayerEntity player = context.getSender();
        if (player != null && type.shouldOpenForPlayer.test(player)) {
            NetworkHooks.openGui(player, type.containerSupplier.get());
        }
    }

    @Override
    public void encode(PacketBuffer buffer) {
        buffer.writeEnum(type);
    }

    public static PacketOpenGui decode(PacketBuffer buffer) {
        return new PacketOpenGui(buffer.readEnum(GuiType.class));
    }

    public enum GuiType {//TODO - 1.18: Evaluate switching this back to MekanismContainerTypes.MODULE_TWEAKER.get().create(id, inv)
        MODULE_TWEAKER(() -> new ContainerProvider(MekanismLang.MODULE_TWEAKER, (id, inv, player) -> new ModuleTweakerContainer(id, inv)),
              ModuleTweakerContainer::hasTweakableItem);

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
