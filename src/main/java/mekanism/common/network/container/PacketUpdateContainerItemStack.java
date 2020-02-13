package mekanism.common.network.container;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for item stacks
 */
public class PacketUpdateContainerItemStack extends PacketUpdateContainer<PacketUpdateContainerItemStack> {

    @Nonnull
    private final ItemStack value;

    public PacketUpdateContainerItemStack(short windowId, short property, @Nonnull ItemStack value) {
        super(windowId, property);
        this.value = value;
    }

    private PacketUpdateContainerItemStack(PacketBuffer buffer) {
        super(buffer);
        this.value = buffer.readItemStack();
    }

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        buffer.writeItemStack(value);
    }

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerItemStack message) {
        container.handleWindowProperty(message.property, message.value);
    }

    public static PacketUpdateContainerItemStack decode(PacketBuffer buf) {
        return new PacketUpdateContainerItemStack(buf);
    }
}