package mekanism.common.content.boiler;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.heat.IMekanismHeatHandler;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.heat.BasicHeatCapacitor;
import mekanism.common.multiblock.MultiblockCache;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;

public class BoilerCache extends MultiblockCache<SynchronizedBoilerData> implements IMekanismFluidHandler, IMekanismGasHandler, IMekanismHeatHandler {

    //Note: We don't care about any restrictions here as it is just for making it be persistent
    private final List<IExtendedFluidTank> fluidTanks = Collections.singletonList(BasicFluidTank.create(Integer.MAX_VALUE, this));
    private final List<IChemicalTank<Gas, GasStack>> gasTanks = Collections.singletonList(BasicGasTank.create(Integer.MAX_VALUE, this));
    public final List<IHeatCapacitor> heatCapacitors = Collections.singletonList(BasicHeatCapacitor.create(HeatAPI.DEFAULT_HEAT_CAPACITY, this));

    @Override
    public void apply(SynchronizedBoilerData data) {
        data.setFluidTankData(fluidTanks);
        data.setGasTankData(gasTanks);
        data.setHeatCapacitorData(heatCapacitors);
    }

    @Override
    public void sync(SynchronizedBoilerData data) {
        List<IExtendedFluidTank> fluidTanksToCopy = data.getFluidTanks(null);
        for (int i = 0; i < fluidTanksToCopy.size(); i++) {
            if (i < fluidTanks.size()) {
                //Just directly set it as we don't have any restrictions on our tanks here
                fluidTanks.get(i).setStack(fluidTanksToCopy.get(i).getFluid());
            }
        }
        List<? extends IChemicalTank<Gas, GasStack>> gasTanksToCopy = data.getGasTanks(null);
        for (int i = 0; i < gasTanksToCopy.size(); i++) {
            if (i < gasTanks.size()) {
                //Just directly set it as we don't have any restrictions on our tanks here
                gasTanks.get(i).setStack(gasTanksToCopy.get(i).getStack());
            }
        }
        List<IHeatCapacitor> heatCapacitorsToCopy = data.getHeatCapacitors(null);
        for (int i = 0; i < heatCapacitorsToCopy.size(); i++) {
            if (i < heatCapacitors.size()) {
                //Just directly set it as we don't have any restrictions on our tanks here
                heatCapacitors.get(i).setHeat(heatCapacitorsToCopy.get(i).getHeat());
                if (heatCapacitors.get(i) instanceof BasicHeatCapacitor) {
                    ((BasicHeatCapacitor) heatCapacitors.get(i)).setHeatCapacity(heatCapacitorsToCopy.get(i).getHeatCapacity(), false);
                }
            }
        }
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        DataHandlerUtils.readTanks(getFluidTanks(null), nbtTags.getList(NBTConstants.FLUID_TANKS, NBT.TAG_COMPOUND));
        DataHandlerUtils.readTanks(getGasTanks(null), nbtTags.getList(NBTConstants.GAS_TANKS, NBT.TAG_COMPOUND));
        DataHandlerUtils.readContainers(getHeatCapacitors(null), nbtTags.getList(NBTConstants.HEAT_CAPACITORS, NBT.TAG_COMPOUND));
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        nbtTags.put(NBTConstants.FLUID_TANKS, DataHandlerUtils.writeTanks(getFluidTanks(null)));
        nbtTags.put(NBTConstants.GAS_TANKS, DataHandlerUtils.writeTanks(getGasTanks(null)));
        nbtTags.put(NBTConstants.HEAT_CAPACITORS, DataHandlerUtils.writeContainers(getHeatCapacitors(null)));
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @Nonnull
    @Override
    public List<? extends IChemicalTank<Gas, GasStack>> getGasTanks(@Nullable Direction side) {
        return gasTanks;
    }

    @Nonnull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(Direction side) {
        return heatCapacitors;
    }

    @Override
    public void onContentsChanged() {}
}