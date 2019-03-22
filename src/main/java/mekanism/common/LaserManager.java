package mekanism.common;

import java.util.List;
import mekanism.api.Coord4D;
import mekanism.api.Pos3D;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

public class LaserManager {

    public static LaserInfo fireLaser(TileEntity from, EnumFacing direction, double energy, World world) {
        return fireLaser(new Pos3D(from).centre().translate(direction, 0.501), direction, energy, world);
    }

    public static LaserInfo fireLaser(Pos3D from, EnumFacing direction, double energy, World world) {
        Pos3D to = from.clone().translate(direction, general.laserRange - 0.002);

        RayTraceResult mop = world.rayTraceBlocks(from, to);

        if (mop != null) {
            to = new Pos3D(mop.hitVec);
            Coord4D toCoord = new Coord4D(mop.getBlockPos(), world);
            TileEntity tile = toCoord.getTileEntity(world);

            if (isReceptor(tile, mop.sideHit)) {
                ILaserReceptor receptor = getReceptor(tile, mop.sideHit);

                if (!(receptor.canLasersDig())) {
                    receptor.receiveLaserEnergy(energy, mop.sideHit);
                }
            }
        }

        from.translateExcludingSide(direction, -0.1);
        to.translateExcludingSide(direction, 0.1);

        boolean foundEntity = false;

        for (Entity e : world.getEntitiesWithinAABB(Entity.class, Pos3D.getAABB(from, to))) {
            foundEntity = true;

            if (!e.isImmuneToFire()) {
                e.setFire((int) (energy / 1000));
            }

            if (energy > 256) {
                e.attackEntityFrom(DamageSource.GENERIC, (float) energy / 1000F);
            }
        }

        return new LaserInfo(mop, foundEntity);
    }

    public static List<ItemStack> breakBlock(Coord4D blockCoord, boolean dropAtBlock, World world, BlockPos laserPos) {
        if (!general.aestheticWorldDamage) {
            return null;
        }

        IBlockState state = blockCoord.getBlockState(world);
        Block blockHit = state.getBlock();

        EntityPlayer dummy = Mekanism.proxy.getDummyPlayer((WorldServer) world, laserPos).get();
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, blockCoord.getPos(), state, dummy);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            return null;
        }

        NonNullList<ItemStack> ret = null;
        if (dropAtBlock) {
            blockHit.dropBlockAsItem(world, blockCoord.getPos(), state, 0);
        } else {
            ret = NonNullList.create();
            blockHit.getDrops(ret, world, blockCoord.getPos(), state, 0);
        }

        blockHit.breakBlock(world, blockCoord.getPos(), state);
        world.setBlockToAir(blockCoord.getPos());
        world.playEvent(2001, blockCoord.getPos(), Block.getStateId(state));

        return ret;
    }

    public static RayTraceResult fireLaserClient(TileEntity from, EnumFacing direction, double energy, World world) {
        return fireLaserClient(new Pos3D(from).centre().translate(direction, 0.501), direction, energy, world);
    }

    public static RayTraceResult fireLaserClient(Pos3D from, EnumFacing direction, double energy, World world) {
        Pos3D to = from.clone().translate(direction, general.laserRange - 0.002);
        RayTraceResult mop = world.rayTraceBlocks(from, to);

        if (mop != null) {
            to = new Pos3D(mop.hitVec);
        }

        from.translate(direction, -0.501);
        Mekanism.proxy.renderLaser(world, from, to, direction, energy);

        return mop;
    }

    public static boolean isReceptor(TileEntity tile, EnumFacing side) {
        return CapabilityUtils.hasCapability(tile, Capabilities.LASER_RECEPTOR_CAPABILITY, side);
    }

    public static ILaserReceptor getReceptor(TileEntity tile, EnumFacing side) {
        return CapabilityUtils.getCapability(tile, Capabilities.LASER_RECEPTOR_CAPABILITY, side);
    }

    public static class LaserInfo {

        public RayTraceResult movingPos;

        public boolean foundEntity;

        public LaserInfo(RayTraceResult mop, boolean b) {
            movingPos = mop;
            foundEntity = b;
        }
    }
}