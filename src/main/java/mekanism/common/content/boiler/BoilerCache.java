package mekanism.common.content.boiler;

import mekanism.common.multiblock.MultiblockCache;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

public class BoilerCache extends MultiblockCache<SynchronizedBoilerData> {

    public FluidStack water;
    public FluidStack steam;
    public double temperature;

    @Override
    public void apply(SynchronizedBoilerData data) {
        data.waterStored = water;
        data.steamStored = steam;
        data.temperature = temperature;
    }

    @Override
    public void sync(SynchronizedBoilerData data) {
        water = data.waterStored;
        steam = data.steamStored;
        temperature = data.temperature;
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        if (nbtTags.hasKey("cachedWater")) {
            water = FluidStack.loadFluidStackFromNBT(nbtTags.getCompoundTag("cachedWater"));
        }
        if (nbtTags.hasKey("cachedSteam")) {
            steam = FluidStack.loadFluidStackFromNBT(nbtTags.getCompoundTag("cachedSteam"));
        }
        temperature = nbtTags.getDouble("temperature");
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        if (water != null) {
            nbtTags.setTag("cachedWater", water.writeToNBT(new CompoundNBT()));
        }
        if (steam != null) {
            nbtTags.setTag("cachedSteam", steam.writeToNBT(new CompoundNBT()));
        }
        nbtTags.setDouble("temperature", temperature);
    }
}