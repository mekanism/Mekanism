package mekanism.common.lib.radiation;

import java.util.Objects;
import java.util.Optional;
import mekanism.api.NBTConstants;
import mekanism.api.radiation.IRadiationSource;
import mekanism.common.config.MekanismConfig;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.NotNull;

public class RadiationSource implements IRadiationSource {

    private final GlobalPos pos;
    /** In Sv/h */
    private double magnitude;

    public RadiationSource(GlobalPos pos, double magnitude) {
        this.pos = pos;
        this.magnitude = magnitude;
    }

    @NotNull
    @Override
    public GlobalPos getPos() {
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

    public static Optional<RadiationSource> load(RegistryOps<Tag> registryOps, CompoundTag tag) {
        Optional<GlobalPos> result = GlobalPos.CODEC.parse(registryOps, tag).result();
        //noinspection OptionalIsPresent - Capturing lambda
        if (result.isPresent()) {
            return Optional.of(new RadiationSource(result.get(), tag.getDouble(NBTConstants.RADIATION)));
        }
        return Optional.empty();
    }

    public CompoundTag write(RegistryOps<Tag> registryOps) {
        CompoundTag tag = (CompoundTag) GlobalPos.CODEC.encodeStart(registryOps, pos).result()
              .orElseGet(CompoundTag::new);
        tag.putDouble(NBTConstants.RADIATION, magnitude);
        return tag;
    }

    @Override
    public boolean equals(Object o) {
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
        return Objects.hash(pos, magnitude);
    }
}