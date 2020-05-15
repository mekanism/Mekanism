package mekanism.generators.common.tile.turbine;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.content.turbine.TurbineUpdateProtocol;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;

public class TileEntityTurbineCasing extends TileEntityMultiblock<TurbineMultiblockData> implements IHasGasMode {

    public TileEntityTurbineCasing() {
        this(GeneratorsBlocks.TURBINE_CASING);
    }

    public TileEntityTurbineCasing(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public void nextMode(int tank) {
        if (tank == 0) {
            getMultiblock().dumpMode = getMultiblock().dumpMode.getNext();
        }
    }

    @Nonnull
    @Override
    public TurbineMultiblockData getNewStructure() {
        return new TurbineMultiblockData(this);
    }

    @Override
    public UpdateProtocol<TurbineMultiblockData> getProtocol() {
        return new TurbineUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<TurbineMultiblockData> getManager() {
        return MekanismGenerators.turbineManager;
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        if (getMultiblock().isFormed() && isRendering) {
            updateTag.putFloat(NBTConstants.SCALE, getMultiblock().prevSteamScale);
            updateTag.putInt(NBTConstants.VOLUME, getMultiblock().getVolume());
            updateTag.putInt(NBTConstants.LOWER_VOLUME, getMultiblock().lowerVolume);
            updateTag.put(NBTConstants.GAS_STORED, getMultiblock().gasTank.getStack().write(new CompoundNBT()));
            updateTag.put(NBTConstants.COMPLEX, NBTUtil.writeBlockPos(getMultiblock().complex));
            updateTag.putFloat(NBTConstants.ROTATION, getMultiblock().clientRotation);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        if (getMultiblock().isFormed() && isRendering) {
            NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> getMultiblock().prevSteamScale = scale);
            NBTUtils.setIntIfPresent(tag, NBTConstants.VOLUME, value -> getMultiblock().setVolume(value));
            NBTUtils.setIntIfPresent(tag, NBTConstants.LOWER_VOLUME, value -> getMultiblock().lowerVolume = value);
            NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED, value -> getMultiblock().gasTank.setStack(value));
            NBTUtils.setBlockPosIfPresent(tag, NBTConstants.COMPLEX, value -> getMultiblock().complex = value);
            NBTUtils.setFloatIfPresent(tag, NBTConstants.ROTATION, value -> getMultiblock().clientRotation = value);
            TurbineMultiblockData.clientRotationMap.put(getMultiblock().inventoryID, getMultiblock().clientRotation);
        }
    }
}