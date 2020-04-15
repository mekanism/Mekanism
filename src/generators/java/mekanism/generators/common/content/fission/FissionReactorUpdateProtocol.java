package mekanism.generators.common.content.fission;

import mekanism.api.Coord4D;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.registries.GeneratorsBlockTypes;
import mekanism.generators.common.tile.fission.TileEntityControlRodAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionFuelAssembly;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorPort;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class FissionReactorUpdateProtocol extends UpdateProtocol<SynchronizedFissionReactorData> {

    public FissionReactorUpdateProtocol(TileEntityFissionReactorCasing tile) {
        super(tile);
    }

    @Override
    protected boolean isValidFrame(int x, int y, int z) {
        return BlockTypeTile.is(pointer.getWorld().getBlockState(new BlockPos(x, y, z)).getBlock(), GeneratorsBlockTypes.FISSION_REACTOR_CASING);
    }

    @Override
    protected boolean isValidInnerNode(int x, int y, int z) {
        if (super.isValidInnerNode(x, y, z)) {
            return true;
        }
        TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), new BlockPos(x, y, z));
        return tile instanceof TileEntityFissionFuelAssembly || tile instanceof TileEntityControlRodAssembly;
    }

    @Override
    protected boolean canForm(SynchronizedFissionReactorData structure) {
        return true;
    }

    @Override
    protected MultiblockManager<SynchronizedFissionReactorData> getManager() {
        return MekanismGenerators.fissionReactorManager;
    }

    @Override
    protected void onStructureCreated(SynchronizedFissionReactorData structure, int origX, int origY, int origZ, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax) {
        for (Coord4D obj : structure.locations) {
            if (MekanismUtils.getTileEntity(pointer.getWorld(), obj.getPos()) instanceof TileEntityFissionReactorPort) {
                structure.portLocations.add(obj);
            }
        }
    }
}
