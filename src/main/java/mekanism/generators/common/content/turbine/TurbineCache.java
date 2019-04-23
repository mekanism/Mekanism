package mekanism.generators.common.content.turbine;

import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

public class TurbineCache extends MultiblockCache<SynchronizedTurbineData> {

    public FluidStack fluid;
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
    public void load(NBTTagCompound nbtTags) {
        if (nbtTags.hasKey("cachedFluid")) {
            fluid = FluidStack.loadFluidStackFromNBT(nbtTags.getCompoundTag("cachedFluid"));
        }

        electricity = nbtTags.getDouble("electricity");
        dumpMode = GasMode.values()[nbtTags.getInteger("dumpMode")];
    }

    @Override
    public void save(NBTTagCompound nbtTags) {
        if (fluid != null) {
            nbtTags.setTag("cachedFluid", fluid.writeToNBT(new NBTTagCompound()));
        }

        nbtTags.setDouble("electricity", electricity);
        nbtTags.setInteger("dumpMode", dumpMode.ordinal());
    }
}
