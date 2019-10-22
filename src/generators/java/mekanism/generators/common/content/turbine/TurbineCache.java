package mekanism.generators.common.content.turbine;

import javax.annotation.Nonnull;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

public class TurbineCache extends MultiblockCache<SynchronizedTurbineData> {

    @Nonnull
    public FluidStack fluid = FluidStack.EMPTY;
    public double electricity;
    public GasMode dumpMode = GasMode.IDLE;

    @Override
    public void apply(SynchronizedTurbineData data) {
        data.fluidStored = fluid;
        data.electricityStored = electricity;
        data.dumpMode = dumpMode;
    }

    @Override
    public void sync(SynchronizedTurbineData data) {
        fluid = data.fluidStored;
        electricity = data.electricityStored;
        dumpMode = data.dumpMode;
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        if (nbtTags.contains("cachedFluid")) {
            fluid = FluidStack.loadFluidStackFromNBT(nbtTags.getCompound("cachedFluid"));
        }
        electricity = nbtTags.getDouble("electricity");
        dumpMode = GasMode.byIndexStatic(nbtTags.getInt("dumpMode"));
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        if (!fluid.isEmpty()) {
            nbtTags.put("cachedFluid", fluid.writeToNBT(new CompoundNBT()));
        }
        nbtTags.putDouble("electricity", electricity);
        nbtTags.putInt("dumpMode", dumpMode.ordinal());
    }
}