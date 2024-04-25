package mekanism.common.network.to_client.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemStackPropertyData extends PropertyData {

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStackPropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          ItemStack.OPTIONAL_STREAM_CODEC, data -> data.value,
          ItemStackPropertyData::new
    );

    @NotNull
    private final ItemStack value;

    public ItemStackPropertyData(short property, @NotNull ItemStack value) {
        super(PropertyType.ITEM_STACK, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }
}