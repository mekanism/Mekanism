package mekanism.common.lib.radiation;

import java.util.Optional;
import mekanism.api.SerializationConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.radiation.IRadiationManager;
import mekanism.api.radiation.IRadiationSource;
import mekanism.common.config.MekanismConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class RadiationSource implements IRadiationSource {

    private final BlockPos pos;
    /** In Sv/h */
    private double magnitude;

    public RadiationSource(BlockPos pos, double magnitude) {
        this.pos = pos;
        this.magnitude = magnitude;
    }

    @NotNull
    @Override
    public BlockPos getPosition() {
        return pos;
    }

    @SuppressWarnings("removal")//backcompat
    @Override
    public GlobalPos getPos() {
        throw new UnsupportedOperationException();
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
        int result = pos.hashCode();
        result = 31 * result + Double.hashCode(magnitude);
        return result;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put(SerializationConstants.POS, NbtUtils.writeBlockPos(pos));
        tag.putDouble(SerializationConstants.RADIATION, magnitude);
        return tag;
    }

    @Nullable
    public static RadiationSource deserializeNBT(CompoundTag nbt) {
        Optional<BlockPos> blockPos = NbtUtils.readBlockPos(nbt, SerializationConstants.POS);
        if (blockPos.isEmpty()) {
            return null;
        }
        return new RadiationSource(blockPos.get(), nbt.getDouble(SerializationConstants.RADIATION));
    }
}