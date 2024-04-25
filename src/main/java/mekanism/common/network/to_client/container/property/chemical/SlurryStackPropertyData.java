package mekanism.common.network.to_client.container.property.chemical;

import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.PropertyType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class SlurryStackPropertyData extends ChemicalStackPropertyData<SlurryStack> {

    public static final StreamCodec<RegistryFriendlyByteBuf, SlurryStackPropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          ChemicalUtils.SLURRY_STACK_STREAM_CODEC, data -> data.value,
          SlurryStackPropertyData::new
    );

    public SlurryStackPropertyData(short property, @NotNull SlurryStack value) {
        super(PropertyType.SLURRY_STACK, property, value);
    }
}