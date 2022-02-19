package mekanism.common.capabilities.heat;

import java.util.Arrays;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.api.heat.HeatAPI;
import mekanism.common.util.EnumUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CachedAmbientTemperature implements DoubleSupplier {

    private final double[] ambientTemperature = new double[EnumUtils.DIRECTIONS.length + 1];
    private final Supplier<World> worldSupplier;
    private final Supplier<BlockPos> positionSupplier;

    public CachedAmbientTemperature(Supplier<World> worldSupplier, Supplier<BlockPos> positionSupplier) {
        this.worldSupplier = worldSupplier;
        this.positionSupplier = positionSupplier;
        Arrays.fill(ambientTemperature, -1);
    }

    @Override
    public double getAsDouble() {
        return getTemperature(null);
    }

    public double getTemperature(@Nullable Direction side) {
        int index = side == null ? EnumUtils.DIRECTIONS.length : side.ordinal();
        double biomeAmbientTemp = ambientTemperature[index];
        if (biomeAmbientTemp == -1) {
            World world = worldSupplier.get();
            if (world == null) {
                return HeatAPI.AMBIENT_TEMP;
            }
            BlockPos pos = positionSupplier.get();
            if (side != null) {
                pos = pos.relative(side);
            }
            return ambientTemperature[index] = HeatAPI.getAmbientTemp(world, pos);
        }
        return biomeAmbientTemp;
    }
}