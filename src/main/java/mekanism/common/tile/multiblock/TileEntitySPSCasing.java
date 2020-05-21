package mekanism.common.tile.multiblock;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.content.sps.SPSMultiblockData;
import mekanism.common.content.sps.SPSStructureValidator;
import mekanism.common.content.sps.SPSUpdateProtocol;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.IStructureValidator;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;

public class TileEntitySPSCasing extends TileEntityMultiblock<SPSMultiblockData> {

    public TileEntitySPSCasing() {
        super(MekanismBlocks.SPS_CASING);
    }

    public TileEntitySPSCasing(IBlockProvider provider) {
        super(provider);
    }


    @Override
    public SPSMultiblockData createMultiblock() {
        return new SPSMultiblockData(this);
    }

    @Override
    public FormationProtocol<SPSMultiblockData> getFormationProtocol() {
        return new SPSUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<SPSMultiblockData> getManager() {
        return Mekanism.spsManager;
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        if (getMultiblock().isFormed() && isMaster) {
            getMultiblock().coilData.write(updateTag);
            updateTag.put(NBTConstants.MIN, NBTUtil.writeBlockPos(getMultiblock().minLocation));
            updateTag.put(NBTConstants.MAX, NBTUtil.writeBlockPos(getMultiblock().maxLocation));
            updateTag.putDouble(NBTConstants.LAST_PROCESSED, getMultiblock().lastProcessed);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        if (getMultiblock().isFormed() && isMaster) {
            getMultiblock().coilData.read(tag);
            getMultiblock().minLocation = NBTUtil.readBlockPos(tag.getCompound(NBTConstants.MIN));
            getMultiblock().maxLocation = NBTUtil.readBlockPos(tag.getCompound(NBTConstants.MAX));
            getMultiblock().lastProcessed = tag.getDouble(NBTConstants.LAST_PROCESSED);
        }
    }

    @Override
    public IStructureValidator validateStructure() {
        return new SPSStructureValidator(getStructure());
    }
}
