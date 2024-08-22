package mekanism.common.lib.transmitter;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import java.util.function.IntFunction;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.IHasTranslationKey.IHasEnumNameTranslationKey;
import mekanism.api.text.ILangEntry;
import mekanism.common.MekanismLang;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

@NothingNullByDefault
public enum TransmissionType implements IHasEnumNameTranslationKey, StringRepresentable {
    ENERGY("EnergyNetwork", "energy", MekanismLang.TRANSMISSION_TYPE_ENERGY, 0),
    FLUID("FluidNetwork", "fluids", MekanismLang.TRANSMISSION_TYPE_FLUID, 1),
    CHEMICAL("ChemicalNetwork", "chemicals", MekanismLang.TRANSMISSION_TYPE_CHEMICALS, 2),//3,4,5 deleted
    ITEM("InventoryNetwork", "items", MekanismLang.TRANSMISSION_TYPE_ITEM, 6),
    HEAT("HeatNetwork", "heat", MekanismLang.TRANSMISSION_TYPE_HEAT, 7);

    public static final Codec<TransmissionType> CODEC;

    //TODO - 1.22 remove backcompat and inline back to StringRepresentable.fromEnum
    static {
        TransmissionType[] values = values();
        Function<String, TransmissionType> nameLookup = StringRepresentable.createNameLookup(values, Function.identity());
        Function<String, TransmissionType> remapper = it -> ("gases".equals(it) || "infuse_types".equals(it) || "pigments".equals(it) || "slurries".equals(it)) ? CHEMICAL : nameLookup.apply(it);
        CODEC = new EnumCodec<>(values, remapper);
    }
    public static final IntFunction<TransmissionType> BY_ID = ByIdMap.continuous(TransmissionType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    public static final StreamCodec<ByteBuf, TransmissionType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, TransmissionType::ordinal);

    private final String name;
    private final String transmission;
    private final ILangEntry langEntry;
    private final int legacyOrdinal;

    TransmissionType(String name, String transmission, ILangEntry langEntry, int legacyOrdinal) {
        this.name = name;
        this.transmission = transmission;
        this.langEntry = langEntry;
        this.legacyOrdinal = legacyOrdinal;
    }

    public String getName() {
        return name;
    }

    public String getTransmission() {
        return transmission;
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

    public int getLegacyOrdinal() {
        return legacyOrdinal;
    }
}