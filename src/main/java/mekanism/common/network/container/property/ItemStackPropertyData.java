package mekanism.common.network.container.property;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

public class ItemStackPropertyData extends PropertyData {

    @Nonnull
    private final ItemStack value;

    public ItemStackPropertyData(short property, @Nonnull ItemStack value) {
        super(PropertyType.ITEM_STACK, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        super.writeToPacket(buffer);
        buffer.writeItemStack(value);
    }
}