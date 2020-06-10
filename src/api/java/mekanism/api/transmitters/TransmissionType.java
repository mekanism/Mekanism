package mekanism.api.transmitters;

import mekanism.api.text.APILang;
import mekanism.api.text.IHasTranslationKey;
import mekanism.api.text.ILangEntry;
import net.minecraft.tileentity.TileEntity;

public enum TransmissionType implements IHasTranslationKey {
    ENERGY("EnergyNetwork", "energy", APILang.TRANSMISSION_TYPE_ENERGY),
    FLUID("FluidNetwork", "fluids", APILang.TRANSMISSION_TYPE_FLUID),
    GAS("GasNetwork", "gases", APILang.TRANSMISSION_TYPE_GAS),
    INFUSION("InfusionNetwork", "infuse_types", APILang.TRANSMISSION_TYPE_INFUSION),
    PIGMENT("PigmentNetwork", "pigments", APILang.TRANSMISSION_TYPE_PIGMENT),
    SLURRY("SlurryNetwork", "slurries", APILang.TRANSMISSION_TYPE_SLURRY),
    ITEM("InventoryNetwork", "items", APILang.TRANSMISSION_TYPE_ITEM),
    HEAT("HeatNetwork", "heat", APILang.TRANSMISSION_TYPE_HEAT);

    private final String name;
    private final String transmission;
    private final APILang langEntry;

    TransmissionType(String name, String transmission, APILang langEntry) {
        this.name = name;
        this.transmission = transmission;
        this.langEntry = langEntry;
    }

    public static boolean checkTransmissionType(ITransmitter sideTile, TransmissionType type) {
        return type.checkTransmissionType(sideTile);
    }

    public static boolean checkTransmissionType(TileEntity tile1, TransmissionType type) {
        return checkTransmissionType(tile1, type, null);
    }

    public static boolean checkTransmissionType(TileEntity tile1, TransmissionType type, TileEntity tile2) {
        return type.checkTransmissionType(tile1, tile2);
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

    public boolean isChemical() {
        return this == GAS || this == INFUSION || this == PIGMENT || this == SLURRY;
    }

    public boolean checkTransmissionType(ITransmitter transmitter) {
        return transmitter.getTransmissionType() == this;
    }

    public boolean checkTransmissionType(TileEntity sideTile, TileEntity currentTile) {
        return sideTile instanceof ITransmitter && ((ITransmitter) sideTile).getTransmissionType() == this;
    }
}