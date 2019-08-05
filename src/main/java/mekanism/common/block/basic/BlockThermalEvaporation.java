package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.IHasModel;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.tile.TileEntityThermalEvaporationBlock;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockThermalEvaporation extends BlockTileDrops implements IHasModel, IHasTileEntity<TileEntityThermalEvaporationBlock> {

    public BlockThermalEvaporation() {
        super(Material.IRON);
        setHardness(5F);
        setResistance(10F);
        setRegistryName(new ResourceLocation(Mekanism.MODID, "thermal_evaporation_block"));
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
        if (!world.isRemote) {
            TileEntity tileEntity = new Coord4D(pos, world).getTileEntity(world);
            if (tileEntity instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        return 9F;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityThermalEvaporationBlock();
    }

    @Nullable
    @Override
    public Class<? extends TileEntityThermalEvaporationBlock> getTileClass() {
        return TileEntityThermalEvaporationBlock.class;
    }
}