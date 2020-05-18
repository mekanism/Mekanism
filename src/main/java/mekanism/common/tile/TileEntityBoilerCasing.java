package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.content.boiler.BoilerUpdateProtocol;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;

public class TileEntityBoilerCasing extends TileEntityMultiblock<BoilerMultiblockData> implements IValveHandler {

    public TileEntityBoilerCasing() {
        this(MekanismBlocks.BOILER_CASING);
    }

    public TileEntityBoilerCasing(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Nonnull
    @Override
    public BoilerMultiblockData createMultiblock() {
        return new BoilerMultiblockData(this);
    }

    @Override
    public BoilerUpdateProtocol getFormationProtocol() {
        return new BoilerUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<BoilerMultiblockData> getManager() {
        return Mekanism.boilerManager;
    }

    @Nonnull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors() {
        return side -> getMultiblock().getHeatCapacitors(side);
    }

    @Override
    public boolean persists(SubstanceType type) {
        //Do not handle heat when it comes to syncing it/saving this tile to disk
        if (type == SubstanceType.HEAT) {
            return false;
        }
        return super.persists(type);
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        if (getMultiblock().isFormed() && isRendering) {
            updateTag.putFloat(NBTConstants.SCALE, getMultiblock().prevWaterScale);
            updateTag.putFloat(NBTConstants.SCALE_ALT, getMultiblock().prevSteamScale);
            updateTag.putInt(NBTConstants.VOLUME, getMultiblock().getWaterVolume());
            updateTag.putInt(NBTConstants.LOWER_VOLUME, getMultiblock().getSteamVolume());
            updateTag.put(NBTConstants.FLUID_STORED, getMultiblock().waterTank.getFluid().writeToNBT(new CompoundNBT()));
            updateTag.put(NBTConstants.GAS_STORED, getMultiblock().steamTank.getStack().write(new CompoundNBT()));
            updateTag.put(NBTConstants.RENDER_Y, getMultiblock().upperRenderLocation.write(new CompoundNBT()));
            updateTag.putBoolean(NBTConstants.HOT, getMultiblock().clientHot);
            writeValves(updateTag);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        if (isRendering && getMultiblock().isFormed()) {
            NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> getMultiblock().prevWaterScale = scale);
            NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE_ALT, scale -> getMultiblock().prevSteamScale = scale);
            NBTUtils.setIntIfPresent(tag, NBTConstants.VOLUME, value -> getMultiblock().setWaterVolume(value));
            NBTUtils.setIntIfPresent(tag, NBTConstants.LOWER_VOLUME, value -> getMultiblock().setSteamVolume(value));
            NBTUtils.setFluidStackIfPresent(tag, NBTConstants.FLUID_STORED, value -> getMultiblock().waterTank.setStack(value));
            NBTUtils.setGasStackIfPresent(tag, NBTConstants.GAS_STORED, value -> getMultiblock().steamTank.setStack(value));
            NBTUtils.setCoord4DIfPresent(tag, NBTConstants.RENDER_Y, value -> getMultiblock().upperRenderLocation = value);
            NBTUtils.setBooleanIfPresent(tag, NBTConstants.HOT, value -> getMultiblock().clientHot = value);
            readValves(tag);
        }
    }
}