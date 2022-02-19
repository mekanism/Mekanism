package mekanism.generators.common.block.fission;

import java.util.Objects;
import mekanism.common.block.prefab.BlockBasicMultiblock;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.lib.radiation.Meltdown.MeltdownExplosion;
import mekanism.common.util.WorldUtils;
import mekanism.generators.common.content.fission.FissionReactorMultiblockData;
import mekanism.generators.common.tile.fission.TileEntityFissionReactorCasing;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockFissionCasing<TILE extends TileEntityFissionReactorCasing> extends BlockBasicMultiblock<TILE> {

    public BlockFissionCasing(BlockTypeTile<TILE> type) {
        super(type);
    }

    @Override
    public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
        if (!world.isClientSide && explosion instanceof MeltdownExplosion) {
            TileEntityFissionReactorCasing tile = WorldUtils.getTileEntity(TileEntityFissionReactorCasing.class, world, pos);
            if (tile != null) {
                FissionReactorMultiblockData multiblock = tile.getMultiblock();
                if (Objects.equals(multiblock.inventoryID, ((MeltdownExplosion) explosion).getMultiblockID())) {
                    multiblock.meltdownHappened(world);
                }
            }
        }
        super.onBlockExploded(state, world, pos, explosion);
    }
}