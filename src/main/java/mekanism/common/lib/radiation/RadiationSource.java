package mekanism.common.lib.radiation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.SerializationConstants;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.radiation.IRadiationSource;
import mekanism.common.config.MekanismConfig;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

public class RadiationSource implements IRadiationSource {

    //TODO - 26.1: Should we apply bounds to what is valid as the radiation level
    public static final Codec<RadiationSource> CODEC = RecordCodecBuilder.create(in -> in.group(
          BlockPos.CODEC.fieldOf(SerializationConstants.POS).forGetter(RadiationSource::getPosition),
          Codec.DOUBLE.fieldOf(SerializationConstants.RADIATION).forGetter(RadiationSource::getMagnitude)
    ).apply(in, RadiationSource::new));

    private final BlockPos pos;
    /** In Sv/h */
    private double magnitude;

    public RadiationSource(BlockPos pos, double magnitude) {
        this.pos = pos;
        this.magnitude = magnitude;
    }

    @Override
    public BlockPos getPosition() {
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
        return magnitude < IRadiationManager.INSTANCE.minRadiationMagnitude();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RadiationSource other = (RadiationSource) o;
        return magnitude == other.magnitude && pos.equals(other.pos);
    }

    @Override
    public int hashCode() {
        int result = pos.hashCode();
        result = 31 * result + Double.hashCode(magnitude);
        return result;
    }
}