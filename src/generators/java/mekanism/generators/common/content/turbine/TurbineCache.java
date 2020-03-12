package mekanism.generators.common.content.turbine;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import mekanism.api.DataHandlerUtils;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.Constants.NBT;

public class TurbineCache extends MultiblockCache<SynchronizedTurbineData> implements IMekanismGasHandler {

    //Note: We don't care about any restrictions here as it is just for making it be persistent
    private final List<IChemicalTank<Gas, GasStack>> gasTanks = Collections.singletonList(BasicGasTank.create(Integer.MAX_VALUE, this));
    public double electricity;
    public GasMode dumpMode = GasMode.IDLE;

    @Override
    public void apply(SynchronizedTurbineData data) {
        data.setTankData(gasTanks);
        data.electricityStored = electricity;
        data.dumpMode = dumpMode;
    }

    @Override
    public void sync(SynchronizedTurbineData data) {
        List<? extends IChemicalTank<Gas, GasStack>> tanksToCopy = data.getGasTanks(null);
        for (int i = 0; i < tanksToCopy.size(); i++) {
            if (i < gasTanks.size()) {
                //Just directly set it as we don't have any restrictions on our tanks here
                gasTanks.get(i).setStack(tanksToCopy.get(i).getStack());
            }
        }
        electricity = data.electricityStored;
        dumpMode = data.dumpMode;
    }

    @Override
    public void load(CompoundNBT nbtTags) {
        DataHandlerUtils.readTanks(getGasTanks(null), nbtTags.getList(NBTConstants.GAS_TANKS, NBT.TAG_COMPOUND));
        electricity = nbtTags.getDouble(NBTConstants.ENERGY_STORED);
        NBTUtils.setEnumIfPresent(nbtTags, NBTConstants.DUMP_MODE, GasMode::byIndexStatic, mode -> dumpMode = mode);
    }

    @Override
    public void save(CompoundNBT nbtTags) {
        nbtTags.put(NBTConstants.GAS_TANKS, DataHandlerUtils.writeTanks(getGasTanks(null)));
        nbtTags.putDouble(NBTConstants.ENERGY_STORED, electricity);
        nbtTags.putInt(NBTConstants.DUMP_MODE, dumpMode.ordinal());
    }

    @Override
    public List<? extends IChemicalTank<Gas, GasStack>> getGasTanks(@Nullable Direction side) {
        return gasTanks;
    }

    @Override
    public void onContentsChanged() {
    }
}