package mekanism.common.network.to_client.container.property.resource;

import mekanism.api.chemical.ChemicalResource;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.PropertyType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class ChemicalResourcePropertyData extends ResourcePropertyData<ChemicalResource> {

    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalResourcePropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          ChemicalResource.STREAM_CODEC, ResourcePropertyData::getValue,
          ChemicalResourcePropertyData::new
    );

    public ChemicalResourcePropertyData(short property, @NotNull ChemicalResource value) {
        super(PropertyType.CHEMICAL_TYPE, property, value);
    }
}