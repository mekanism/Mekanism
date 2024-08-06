package mekanism.common.network.to_client.container.property.chemical;

import mekanism.api.chemical.ChemicalStack;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.PropertyType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class ChemicalStackPropertyData extends PropertyData {

    public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalStackPropertyData> STREAM_CODEC = StreamCodec.composite(
          ByteBufCodecs.SHORT, PropertyData::getProperty,
          ChemicalStack.OPTIONAL_STREAM_CODEC, data -> data.value,
          ChemicalStackPropertyData::new
    );

    private final ChemicalStack value;

    public ChemicalStackPropertyData(short property, ChemicalStack value) {
        super(PropertyType.CHEMICAL_STACK, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }
}