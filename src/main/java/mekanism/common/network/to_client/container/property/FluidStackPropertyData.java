package mekanism.common.network.to_client.container.property;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class FluidStackPropertyData extends PropertyData {

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidStackPropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          FluidStack.OPTIONAL_STREAM_CODEC, data -> data.value,
          FluidStackPropertyData::new
    );

    @NotNull
    private final FluidStack value;

    public FluidStackPropertyData(short property, @NotNull FluidStack value) {
        super(PropertyType.FLUID_STACK, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }
}