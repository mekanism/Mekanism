package mekanism.common.network.to_client.container.property.chemical;

import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.PropertyType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class InfusionStackPropertyData extends ChemicalStackPropertyData<InfusionStack> {

    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionStackPropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          ChemicalUtils.INFUSION_STACK_STREAM_CODEC, data -> data.value,
          InfusionStackPropertyData::new
    );

    public InfusionStackPropertyData(short property, @NotNull InfusionStack value) {
        super(PropertyType.INFUSION_STACK, property, value);
    }
}