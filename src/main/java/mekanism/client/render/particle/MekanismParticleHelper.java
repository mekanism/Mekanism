package mekanism.client.render.particle;

import mekanism.api.EnumColor;
import mekanism.common.base.ITieredTile;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MekanismParticleHelper {

    //From net.minecraft.client.particle.ParticleManager
    public static boolean addBlockHitEffects(World world, BlockPos pos, EnumFacing side, ParticleManager manager) {
        TileEntity tile = MekanismUtils.getTileEntitySafe(world, pos);
        EnumColor color = null;
        IBlockState iblockstate = world.getBlockState(pos);
        //Makes it so transmitters don't need to change their tier
        iblockstate = iblockstate.getBlock().getActualState(iblockstate, world, pos);
        if (tile instanceof ITieredTile) {
            color = ((ITieredTile) tile).getTier().getColor();
        }

        if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            AxisAlignedBB axisalignedbb = iblockstate.getBoundingBox(world, pos);
            double d0 = (double) i + world.rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX
                  - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minX;
            double d1 = (double) j + world.rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY
                  - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minY;
            double d2 = (double) k + world.rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ
                  - 0.20000000298023224D) + 0.10000000149011612D + axisalignedbb.minZ;

            if (side == EnumFacing.DOWN) {
                d1 = (double) j + axisalignedbb.minY - 0.10000000149011612D;
            }

            if (side == EnumFacing.UP) {
                d1 = (double) j + axisalignedbb.maxY + 0.10000000149011612D;
            }

            if (side == EnumFacing.NORTH) {
                d2 = (double) k + axisalignedbb.minZ - 0.10000000149011612D;
            }

            if (side == EnumFacing.SOUTH) {
                d2 = (double) k + axisalignedbb.maxZ + 0.10000000149011612D;
            }

            if (side == EnumFacing.WEST) {
                d0 = (double) i + axisalignedbb.minX - 0.10000000149011612D;
            }

            if (side == EnumFacing.EAST) {
                d0 = (double) i + axisalignedbb.maxX + 0.10000000149011612D;
            }

            manager.addEffect(new TieredParticleDigging(world, d0, d1, d2, 0, 0, 0, iblockstate, color)
                  .setBlockPos(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
        }
        return true;
    }
}