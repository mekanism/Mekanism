package mekanism.common.network.to_client.container.property.chemical;

import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.PropertyType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class PigmentStackPropertyData extends ChemicalStackPropertyData<PigmentStack> {

    public static final StreamCodec<RegistryFriendlyByteBuf, PigmentStackPropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          ChemicalUtils.PIGMENT_STACK_STREAM_CODEC, data -> data.value,
          PigmentStackPropertyData::new
    );

    public PigmentStackPropertyData(short property, @NotNull PigmentStack value) {
        super(PropertyType.PIGMENT_STACK, property, value);
    }
}