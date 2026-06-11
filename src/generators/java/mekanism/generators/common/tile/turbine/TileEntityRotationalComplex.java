package mekanism.generators.common.tile.turbine;

import java.util.UUID;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class TileEntityRotationalComplex extends TileEntityInternalMultiblock {

    public TileEntityRotationalComplex(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.ROTATIONAL_COMPLEX, pos, state);
    }

    @Override
    protected void multiblockChanged(LevelReader level, @Nullable UUID old) {
        super.multiblockChanged(level, old);
        if (!level.isClientSide()) {
            TileEntityTurbineRotor tile = WorldUtils.getTileEntity(TileEntityTurbineRotor.class, level, getBlockPos().below());
            if (tile != null) {
                tile.updateRotors(level);
            }
        } else if (getMultiblockUUID() == null && old != null) {
            TurbineMultiblockData.clientRotationMap.removeFloat(old);
        }
    }
}