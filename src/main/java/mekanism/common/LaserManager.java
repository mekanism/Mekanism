package mekanism.common;

import java.util.List;
import mekanism.api.Coord4D;
import mekanism.api.Pos3D;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.particle.LaserParticleData;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.WorldEvents;
import net.minecraftforge.event.world.BlockEvent;

public class LaserManager {

    public static LaserInfo fireLaser(TileEntity source, Direction direction, double energy, World world) {
        Pos3D from = new Pos3D(source).centre().translate(direction, 0.501);
        ServerWorld serverWorld = (ServerWorld) world;
        Pos3D to = from.clone().translate(direction, MekanismConfig.general.laserRange.get() - 0.002);
        PlayerEntity dummy = Mekanism.proxy.getDummyPlayer(serverWorld, new BlockPos(from)).get();
        //TODO: Verify this is correct
        BlockRayTraceResult mop = world.rayTraceBlocks(new RayTraceContext(from, to, BlockMode.COLLIDER, FluidMode.NONE, dummy));
        if (mop.getType() != Type.MISS) {
            to = new Pos3D(mop.getHitVec());
            Coord4D toCoord = new Coord4D(mop.getPos(), world);
            TileEntity tile = toCoord.getTileEntity(world);
            CapabilityUtils.getCapabilityHelper(tile, Capabilities.LASER_RECEPTOR_CAPABILITY, mop.getFace()).ifPresent(receptor -> {
                if (!receptor.canLasersDig()) {
                    receptor.receiveLaserEnergy(energy, mop.getFace());
                }
            });
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

        //Tell the clients to render the laser
        LaserParticleData data = new LaserParticleData(direction, to.distance(from), energy);
        serverWorld.getPlayers().forEach(player ->
              serverWorld.spawnParticle(player, data, true, from.x, from.y, from.z, 1, 0.0D, 0.0D, 0.0D, 0));
        return new LaserInfo(mop, foundEntity);
    }

    public static List<ItemStack> breakBlock(Coord4D blockCoord, boolean dropAtBlock, World world, BlockPos laserPos) {
        if (!MekanismConfig.general.aestheticWorldDamage.get()) {
            return null;
        }

        BlockState state = blockCoord.getBlockState(world);
        Block blockHit = state.getBlock();
        PlayerEntity dummy = Mekanism.proxy.getDummyPlayer((ServerWorld) world, laserPos).get();
        BlockPos pos = blockCoord.getPos();
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, dummy);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return null;
        }
        NonNullList<ItemStack> ret = null;
        if (dropAtBlock) {
            Block.spawnDrops(state, world, pos, world.getTileEntity(pos));
        } else {
            ret = NonNullList.create();
            //TODO: Check this is correct/handle tile entity
            ret.addAll(Block.getDrops(state, (ServerWorld) world, pos, MekanismUtils.getTileEntity(world, pos)));
        }
        //TODO: Check this
        blockHit.onReplaced(state, world, pos, Blocks.AIR.getDefaultState(), false);
        world.removeBlock(pos, false);
        world.playEvent(WorldEvents.BREAK_BLOCK_EFFECTS, pos, Block.getStateId(state));
        return ret;
    }

    //TODO: Remove, have server tell client about the effects
    @Deprecated
    public static BlockRayTraceResult fireLaserClient(TileEntity source, Direction direction, double energy, World world) {
        Pos3D from = new Pos3D(source).centre().translate(direction, 0.501);
        Pos3D to = from.clone().translate(direction, MekanismConfig.general.laserRange.get() - 0.002);
        //TODO: Verify this is correct
        return world.rayTraceBlocks(new RayTraceContext(from, to, BlockMode.COLLIDER, FluidMode.NONE, Minecraft.getInstance().player));
    }

    //TODO: Should this be removed?
    public static boolean isReceptor(TileEntity tile, Direction side) {
        return tile != null && tile.getCapability(Capabilities.LASER_RECEPTOR_CAPABILITY, side).isPresent();
    }

    public static class LaserInfo {

        public BlockRayTraceResult movingPos;

        public boolean foundEntity;

        public LaserInfo(BlockRayTraceResult mop, boolean b) {
            movingPos = mop;
            foundEntity = b;
        }
    }
}