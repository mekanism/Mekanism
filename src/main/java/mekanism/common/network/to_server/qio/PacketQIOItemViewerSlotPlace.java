package mekanism.common.network.to_server.qio;

import mekanism.common.Mekanism;
import mekanism.common.content.qio.QIOFrequency;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PacketQIOItemViewerSlotPlace(int count) implements IMekanismPacket<PlayPayloadContext> {

    public static final ResourceLocation ID = Mekanism.rl("qio_place");

    public PacketQIOItemViewerSlotPlace(FriendlyByteBuf buffer) {
        this(buffer.readVarInt());
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void handle(PlayPayloadContext context) {
        QIOItemViewerContainer container = PacketUtils.container(context, QIOItemViewerContainer.class).orElse(null);
        if (container != null) {
            QIOFrequency freq = container.getFrequency();
            if (freq != null) {
                ItemStack curStack = container.getCarried();
                //Count should always be greater than zero but validate against invalid packets
                if (!curStack.isEmpty() && count > 0) {
                    ItemStack toAdd;
                    if (count < curStack.getCount()) {//Only adding part of the stack
                        toAdd = curStack.copyWithCount(count);
                    } else {//Try to add the full held stack
                        toAdd = curStack;
                    }
                    ItemStack rejects = freq.addItem(toAdd);
                    //Calculate actual amount we were able to add of what we tried to add
                    int placed = toAdd.getCount() - rejects.getCount();
                    if (placed > 0) {
                        //If we added any from the held stack, shrink the held stack (which will cause it to be updated on the client)
                        curStack.shrink(placed);
                    }
                }
            }
        }
    }

    @Override
    public void write(@NotNull FriendlyByteBuf buffer) {
        buffer.writeVarInt(count);
    }
}
