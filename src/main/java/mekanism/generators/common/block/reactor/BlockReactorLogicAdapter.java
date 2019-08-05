package mekanism.generators.common.block.reactor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.tile.reactor.TileEntityReactorLogicAdapter;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockReactorLogicAdapter extends Block implements IHasGui, IHasTileEntity<TileEntityReactorLogicAdapter> {

    public BlockReactorLogicAdapter() {
        super(Material.IRON);
        setHardness(3.5F);
        setResistance(8F);
        setRegistryName(new ResourceLocation(MekanismGenerators.MODID, "reactor_logic_adapter"));
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }
        TileEntityMekanism tileEntity = (TileEntityMekanism) world.getTileEntity(pos);
        if (tileEntity.tryWrench(state, player, hand, () -> new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos)) != WrenchResult.PASS) {
            return true;
        }
        if (tileEntity.openGui(player)) {
            return true;
        }
        return false;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityReactorLogicAdapter();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    @Deprecated
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity tile = MekanismUtils.getTileEntitySafe(world, pos);
        if (tile instanceof TileEntityReactorLogicAdapter) {
            return ((TileEntityReactorLogicAdapter) tile).checkMode() ? 15 : 0;
        }
        return 0;
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public int getGuiID() {
        return 15;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityReactorLogicAdapter> getTileClass() {
        return TileEntityReactorLogicAdapter.class;
    }
}