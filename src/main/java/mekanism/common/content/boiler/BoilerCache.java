package mekanism.common.content.boiler;

import javax.annotation.Nonnull;
import mekanism.common.multiblock.MultiblockCache;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

public class BoilerCache extends MultiblockCache<SynchronizedBoilerData> {

    @Nonnull
    public FluidStack water = FluidStack.EMPTY;
    @Nonnull
    public FluidStack steam = FluidStack.EMPTY;
    public double temperature;

    @Override
    public void apply(SynchronizedBoilerData data) {
        data.waterTank.setStack(water);
        data.steamTank.setStack(steam);
        data.temperature = temperature;
    }

    @Override
    public void sync(SynchronizedBoilerData data) {
        water = data.waterTank.getFluid();
        steam = data.steamTank.getFluid();
        temperature = data.temperature;
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        if (nbtTags.contains("cachedWater")) {
            water = FluidStack.loadFluidStackFromNBT(nbtTags.getCompound("cachedWater"));
        }
        if (nbtTags.contains("cachedSteam")) {
            steam = FluidStack.loadFluidStackFromNBT(nbtTags.getCompound("cachedSteam"));
        }
        temperature = nbtTags.getDouble("temperature");
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        if (!water.isEmpty()) {
            nbtTags.put("cachedWater", water.writeToNBT(new CompoundNBT()));
        }
        if (!steam.isEmpty()) {
            nbtTags.put("cachedSteam", steam.writeToNBT(new CompoundNBT()));
        }
        nbtTags.putDouble("temperature", temperature);
    }
}