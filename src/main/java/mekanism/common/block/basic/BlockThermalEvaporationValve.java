package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IHasModel;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.tile.TileEntityThermalEvaporationValve;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockThermalEvaporationValve extends BlockMekanism implements IHasModel, IHasTileEntity<TileEntityThermalEvaporationValve>, ISupportsComparator,
      IHasDescription {

    public BlockThermalEvaporationValve() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5F, 10F));
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (tile != null) {
                tile.onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        return 9F;
    }

    @Override
    public TileEntityType<TileEntityThermalEvaporationValve> getTileType() {
        return MekanismTileEntityTypes.THERMAL_EVAPORATION_VALVE.getTileEntityType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_DYNAMIC_VALVE;
    }
}