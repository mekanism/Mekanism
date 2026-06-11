package mekanism.common.network.to_client.container.property.resource;

import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.PropertyType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class FluidResourcePropertyData extends ResourcePropertyData<FluidResource> {

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidResourcePropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          FluidResource.STREAM_CODEC, ResourcePropertyData::getValue,
          FluidResourcePropertyData::new
    );

    public FluidResourcePropertyData(short property, FluidResource value) {
        super(PropertyType.FLUID_TYPE, property, value);
    }
}