package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.common.block.BlockBasic;
import mekanism.common.tile.TileEntityBoilerCasing;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class BlockBoilerCasing extends BlockBasic {

    public BlockBoilerCasing() {
        super("boiler_casing");
    }

    @Override
    public boolean hasDescription() {
        return true;
    }

    @Override
    public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, SpawnPlacementType type) {
        TileEntityMultiblock<?> tileEntity = (TileEntityMultiblock<?>) MekanismUtils.getTileEntitySafe(world, pos);
        if (tileEntity != null) {
            if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
                if (tileEntity.structure != null) {
                    return false;
                }
            } else if (tileEntity.clientHasStructure) {
                return false;
            }
        }
        return super.canCreatureSpawn(state, world, pos, type);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityBoilerCasing();
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
}