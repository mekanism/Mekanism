package mekanism.common.network.to_client.container.property;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

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
    public void writeToPacket(FriendlyByteBuf buffer) {
        super.writeToPacket(buffer);
        buffer.writeItem(value);
    }
}