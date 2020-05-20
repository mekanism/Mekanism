package mekanism.common.tile.multiblock;

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

    @Override
    public IStructureValidator validateStructure() {
        return new SPSStructureValidator(getStructure());
    }
}
