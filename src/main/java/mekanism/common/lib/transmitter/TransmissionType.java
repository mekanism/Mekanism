package mekanism.common.lib.transmitter;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import mekanism.api.text.IHasTranslationKey.IHasEnumNameTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum TransmissionType implements IHasEnumNameTranslationKey, StringRepresentable {
    ENERGY("EnergyNetwork", "energy", MekanismLang.TRANSMISSION_TYPE_ENERGY),
    FLUID("FluidNetwork", "fluids", MekanismLang.TRANSMISSION_TYPE_FLUID),
    CHEMICAL("ChemicalNetwork", "chemicals", MekanismLang.TRANSMISSION_TYPE_CHEMICALS),
    ITEM("InventoryNetwork", "items", MekanismLang.TRANSMISSION_TYPE_ITEM),
    HEAT("HeatNetwork", "heat", MekanismLang.TRANSMISSION_TYPE_HEAT);

    public static final Codec<TransmissionType> CODEC = StringRepresentable.fromEnum(TransmissionType::values);
    public static final IntFunction<TransmissionType> BY_ID = ByIdMap.continuous(TransmissionType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, TransmissionType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, TransmissionType::ordinal);

    private final String name;
    private final String transmission;
    private final ILangEntry langEntry;

    TransmissionType(String name, String transmission, ILangEntry langEntry) {
        this.name = name;
        this.transmission = transmission;
        this.langEntry = langEntry;
    }

    public String getName() {
        return name;
    }

    public String getTransmission() {
        return transmission;
    }

    public Identifier guiTexture() {
        return Mekanism.rl("transmission/" + transmission);
    }

    public ILangEntry getLangEntry() {
        return langEntry;
    }

    @Override
    public String getTranslationKey() {
        return langEntry.getTranslationKey();
    }

    public boolean checkTransmissionType(Transmitter<?, ?, ?> transmitter) {
        return transmitter.getSupportedTransmissionTypes().contains(this);
    }

    public boolean checkTransmissionType(TileEntityTransmitter transmitter) {
        return checkTransmissionType(transmitter.getTransmitter());
    }

    @Override
    public String getSerializedName() {
        return transmission;
    }
}