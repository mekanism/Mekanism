package mekanism.generators.common.content.turbine;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;

public class TurbineCache extends MultiblockCache<SynchronizedTurbineData> implements IMekanismFluidHandler {

    //Note: We don't care about any restrictions here as it is just for making it be persistent
    private final List<IExtendedFluidTank> fluidTanks = Collections.singletonList(BasicFluidTank.create(Integer.MAX_VALUE, this));
    public double electricity;
    public GasMode dumpMode = GasMode.IDLE;

    @Override
    public void apply(SynchronizedTurbineData data) {
        data.setTankData(fluidTanks);
        data.electricityStored = electricity;
        data.dumpMode = dumpMode;
    }

    @Override
    public void sync(SynchronizedTurbineData data) {
        List<IExtendedFluidTank> tanksToCopy = data.getFluidTanks(null);
        for (int i = 0; i < tanksToCopy.size(); i++) {
            if (i < fluidTanks.size()) {
                //Just directly set it as we don't have any restrictions on our tanks here
                fluidTanks.get(i).setStack(tanksToCopy.get(i).getFluid());
            }
        }
        electricity = data.electricityStored;
        dumpMode = data.dumpMode;
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        DataHandlerUtils.readTanks(getFluidTanks(null), nbtTags.getList(NBTConstants.FLUID_TANKS, NBT.TAG_COMPOUND));
        electricity = nbtTags.getDouble(NBTConstants.ENERGY_STORED);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.DUMP_MODE, GasMode::byIndexStatic, mode -> dumpMode = mode);
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        nbtTags.put(NBTConstants.FLUID_TANKS, DataHandlerUtils.writeTanks(getFluidTanks(null)));
        nbtTags.putDouble(NBTConstants.ENERGY_STORED, electricity);
        nbtTags.putInt(NBTConstants.DUMP_MODE, dumpMode.ordinal());
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