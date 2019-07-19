package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockBasic;
import mekanism.common.tile.TileEntityThermalEvaporationController;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockThermalEvaporationController extends BlockBasic {

    public BlockThermalEvaporationController() {
        super("thermal_evaporation_controller", Plane.HORIZONTAL);
    }

    @Override
    public boolean hasDescription() {
        return true;
    }

    @Override
    public boolean hasActiveTexture() {
        return true;
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
        return new TileEntityThermalEvaporationController();
    }

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!entityplayer.isSneaking()) {
            if (!world.isRemote) {
                entityplayer.openGui(Mekanism.instance, 33, world, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        }
        return false;
    }
}