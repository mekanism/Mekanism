package mekanism.common.lib.radiation;

import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.NBTConstants;
import mekanism.api.radiation.IRadiationSource;
import mekanism.common.config.MekanismConfig;
import net.minecraft.nbt.CompoundNBT;

public class RadiationSource implements IRadiationSource {

    private final Coord4D pos;
    /** In Sv/h */
    private double magnitude;

    public RadiationSource(Coord4D pos, double magnitude) {
        this.pos = pos;
        this.magnitude = magnitude;
    }

    @Nonnull
    @Override
    public Coord4D getPos() {
        return pos;
    }

    @Override
    public double getMagnitude() {
        return magnitude;
    }

    @Override
    public void radiate(double magnitude) {
        this.magnitude += magnitude;
    }

    @Override
    public boolean decay() {
        magnitude *= MekanismConfig.general.radiationSourceDecayRate.get();
        return magnitude < RadiationManager.MIN_MAGNITUDE;
    }

    public static RadiationSource load(CompoundNBT tag) {
        return new RadiationSource(Coord4D.read(tag), tag.getDouble(NBTConstants.RADIATION));
    }

    public void write(CompoundNBT tag) {
        pos.write(tag);
        tag.putDouble(NBTConstants.RADIATION, magnitude);
    }
}
