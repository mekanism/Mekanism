package mekanism.api.transmitters;

import mekanism.api.text.IHasTranslationKey;
import net.minecraft.tileentity.TileEntity;

public enum TransmissionType implements IHasTranslationKey {
    ENERGY("EnergyNetwork", "energy"),
    FLUID("FluidNetwork", "fluids"),
    GAS("GasNetwork", "gases"),
    ITEM("InventoryNetwork", "items"),
    HEAT("HeatNetwork", "heat");

    private String name;
    private String transmission;

    TransmissionType(String n, String t) {
        name = n;
        transmission = t;
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

    @Override
    public String getTranslationKey() {
        return "transmission.mekanism." + getTransmission();
    }

    public boolean checkTransmissionType(ITransmitter transmitter) {
        return transmitter.getTransmissionType() == this;
    }

    public boolean checkTransmissionType(TileEntity sideTile, TileEntity currentTile) {
        return sideTile instanceof ITransmitter && ((ITransmitter) sideTile).getTransmissionType() == this;
    }
}