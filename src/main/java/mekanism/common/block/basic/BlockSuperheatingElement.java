package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateActive;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.tile.TileEntitySuperheatingElement;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockSuperheatingElement extends BlockTileDrops implements IStateActive, IHasTileEntity<TileEntitySuperheatingElement> {

    public BlockSuperheatingElement() {
        super(Material.IRON);
        setHardness(5F);
        setResistance(10F);
        setRegistryName(new ResourceLocation(Mekanism.MODID, "superheating_element"));
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return BlockStateHelper.getBlockState(this);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        //TODO
        return 0;
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState getActualState(@Nonnull BlockState state, IWorldReader world, BlockPos pos) {
        return BlockStateHelper.getActualState(this, state, MekanismUtils.getTileEntitySafe(world, pos));
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
        if (!world.isRemote) {
            TileEntity tileEntity = new Coord4D(pos, world).getTileEntity(world);
            if (tileEntity instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull BlockState state) {
        return new TileEntitySuperheatingElement();
    }

    @Override
    public int getLightValue(BlockState state, IWorldReader world, BlockPos pos) {
        return isActive(state, world, pos) ? 15 : super.getLightValue(state, world, pos);
    }

    @Override
    public boolean isActive(@Nonnull TileEntity tile) {
        if (tile instanceof TileEntitySuperheatingElement) {
            //Should be true
            TileEntitySuperheatingElement heating = (TileEntitySuperheatingElement) tile;
            if (heating.multiblockUUID != null && SynchronizedBoilerData.clientHotMap.get(heating.multiblockUUID) != null) {
                return SynchronizedBoilerData.clientHotMap.get(heating.multiblockUUID);
            }
        }
        return false;
    }

    @Nullable
    @Override
    public Class<? extends TileEntitySuperheatingElement> getTileClass() {
        return TileEntitySuperheatingElement.class;
    }
}