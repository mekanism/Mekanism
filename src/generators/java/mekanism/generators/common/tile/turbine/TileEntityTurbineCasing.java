package mekanism.generators.common.tile.turbine;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.multiblock.CuboidStructureValidator;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.IStructureValidator;
import mekanism.common.lib.multiblock.MultiblockManager;
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
    public TurbineMultiblockData createMultiblock() {
        return new TurbineMultiblockData(this);
    }

    @Override
    public FormationProtocol<TurbineMultiblockData> getFormationProtocol() {
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
        if (getMultiblock().isFormed() && isMaster) {
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
        if (getMultiblock().isFormed() && isMaster) {
            NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> getMultiblock().prevSteamScale = scale);
            NBTUtils.setIntIfPresent(tag, NBTConstants.VOLUME, value -> getMultiblock().setVolume(value));
            NBTUtils.setIntIfPresent(tag, NBTConstants.LOWER_VOLUME, value -> getMultiblock().lowerVolume = value);
            NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED, value -> getMultiblock().gasTank.setStack(value));
            NBTUtils.setBlockPosIfPresent(tag, NBTConstants.COMPLEX, value -> getMultiblock().complex = value);
            NBTUtils.setFloatIfPresent(tag, NBTConstants.ROTATION, value -> getMultiblock().clientRotation = value);
            TurbineMultiblockData.clientRotationMap.put(getMultiblock().inventoryID, getMultiblock().clientRotation);
        }
    }

    @Override
    public IStructureValidator validateStructure() {
        return new CuboidStructureValidator(getStructure(), new VoxelCuboid(3, 3, 3), new VoxelCuboid(17, 18, 17));
    }
}