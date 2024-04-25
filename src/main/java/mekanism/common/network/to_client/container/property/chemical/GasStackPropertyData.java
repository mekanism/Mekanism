package mekanism.common.network.to_client.container.property.chemical;

import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.GasStack;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.PropertyType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class GasStackPropertyData extends ChemicalStackPropertyData<GasStack> {

    public static final StreamCodec<RegistryFriendlyByteBuf, GasStackPropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          ChemicalUtils.GAS_STACK_STREAM_CODEC, data -> data.value,
          GasStackPropertyData::new
    );

    public GasStackPropertyData(short property, @NotNull GasStack value) {
        super(PropertyType.GAS_STACK, property, value);
    }
}