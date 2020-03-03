package mekanism.common.content.boiler;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.DataHandlerUtils;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.multiblock.MultiblockCache;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;

public class BoilerCache extends MultiblockCache<SynchronizedBoilerData> implements IMekanismFluidHandler {

    //Note: We don't care about any restrictions here as it is just for making it be persistent
    private final List<IExtendedFluidTank> fluidTanks = Arrays.asList(BasicFluidTank.create(Integer.MAX_VALUE, this), BasicFluidTank.create(Integer.MAX_VALUE, this));
    public double temperature;

    @Override
    public void apply(SynchronizedBoilerData data) {
        data.setTankData(fluidTanks);
        data.temperature = temperature;
    }

    @Override
    public void sync(SynchronizedBoilerData data) {
        List<IExtendedFluidTank> tanksToCopy = data.getFluidTanks(null);
        for (int i = 0; i < tanksToCopy.size(); i++) {
            if (i < fluidTanks.size()) {
                //Just directly set it as we don't have any restrictions on our tanks here
                fluidTanks.get(i).setStack(tanksToCopy.get(i).getFluid());
            }
        }
        temperature = data.temperature;
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        DataHandlerUtils.readTanks(getFluidTanks(null), nbtTags.getList("FluidTanks", NBT.TAG_COMPOUND));
        temperature = nbtTags.getDouble("temperature");
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        nbtTags.put("FluidTanks", DataHandlerUtils.writeTanks(getFluidTanks(null)));
        nbtTags.putDouble("temperature", temperature);
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @Override
    public void onContentsChanged() {
    }
}