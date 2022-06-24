package mekanism.generators.common.tile.turbine;

import java.util.UUID;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TileEntityRotationalComplex extends TileEntityInternalMultiblock {

    public TileEntityRotationalComplex(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.ROTATIONAL_COMPLEX, pos, state);
    }

    @Override
    protected void multiblockChanged(@Nullable UUID old) {
        super.multiblockChanged(old);
        if (!isRemote()) {
            TileEntityTurbineRotor tile = WorldUtils.getTileEntity(TileEntityTurbineRotor.class, getLevel(), getBlockPos().below());
            if (tile != null) {
                tile.updateRotors();
            }
        } else if (getMultiblockUUID() == null && old != null) {
            TurbineMultiblockData.clientRotationMap.removeFloat(old);
        }
    }
}