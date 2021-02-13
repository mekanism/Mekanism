package mekanism.common.block.prefab;

import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeParticleFX.Particle;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.block.attribute.Attributes.AttributeRedstoneEmitter;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.tile.interfaces.IActiveState;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockTile<TILE extends TileEntityMekanism, TYPE extends BlockTypeTile<TILE>> extends BlockBase<TYPE> implements IHasTileEntity<TILE> {

    public BlockTile(TYPE type) {//TODO - 10.1: Figure out what the resistance should be (it used to be different in 1.12)
        this(type, AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 16).setRequiresTool());
    }

    public BlockTile(TYPE type, AbstractBlock.Properties properties) {
        super(type, properties);
    }

    @Override
    public TileEntityType<TILE> getTileType() {
        return type.getTileType();
    }

    @Nonnull
    @Override
    @Deprecated
    public ActionResultType onBlockActivated(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand,
          @Nonnull BlockRayTraceResult hit) {
        TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return ActionResultType.PASS;
        }
        if (world.isRemote) {
            return genericClientActivated(player, hand, hit);
        }
        if (tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return ActionResultType.SUCCESS;
        }
        return type.has(AttributeGui.class) ? tile.openGui(player) : ActionResultType.PASS;
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return Math.max(super.getLightValue(state, world, pos), getTileLight(world, pos));
    }

    protected int getTileLight(@Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        if (MekanismConfig.client.enableAmbientLighting.get() && type.has(AttributeStateActive.class)) {
            TileEntity tile = WorldUtils.getTileEntity(world, pos);
            if (tile instanceof IActiveState && ((IActiveState) tile).lightUpdate() && ((IActiveState) tile).getActive()) {
                return ((IActiveState) tile).getActiveLightValue();
            }
        }
        return 0;
    }

    @Override
    protected float getPlayerRelativeBlockHardness(@Nonnull BlockState state, @Nonnull PlayerEntity player, @Nonnull IBlockReader world, @Nonnull BlockPos pos,
          @Nullable TileEntity tile) {
        return SecurityUtils.canAccess(player, tile) ? super.getPlayerRelativeBlockHardness(state, player, world, pos, tile) : 0.0F;
    }

    @Override
    public void animateTick(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Random random) {
        super.animateTick(state, world, pos, random);
        if (Attribute.has(state.getBlock(), AttributeParticleFX.class)) {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (tile != null && tile.getActive()) {
                for (Function<Random, Particle> particleFunction : type.get(AttributeParticleFX.class).getParticleFunctions()) {
                    Particle particle = particleFunction.apply(random);
                    Vector3d particlePos = particle.getPos();
                    if (tile.getDirection() == Direction.WEST) {
                        particlePos = particlePos.rotateYaw(90);
                    } else if (tile.getDirection() == Direction.EAST) {
                        particlePos = particlePos.rotateYaw(270);
                    } else if (tile.getDirection() == Direction.NORTH) {
                        particlePos = particlePos.rotateYaw(180);
                    }
                    particlePos = particlePos.add(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    world.addParticle(particle.getType(), particlePos.x, particlePos.y, particlePos.z, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    @Override
    @Deprecated
    public void neighborChanged(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Block neighborBlock, @Nonnull BlockPos neighborPos,
          boolean isMoving) {
        if (!world.isRemote) {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (tile != null) {
                tile.onNeighborChange(neighborBlock, neighborPos);
            }
        }
    }

    @Override
    @Deprecated
    public boolean canProvidePower(@Nonnull BlockState state) {
        return type.has(AttributeRedstoneEmitter.class);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return type.has(AttributeRedstoneEmitter.class) || super.canConnectRedstone(state, world, pos, side);
    }

    @Override
    @Deprecated
    public int getWeakPower(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull Direction side) {
        if (type.has(AttributeRedstoneEmitter.class)) {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            return type.get(AttributeRedstoneEmitter.class).getRedstoneLevel(tile);
        }
        return super.getWeakPower(state, world, pos, side);
    }

    public static class BlockTileModel<TILE extends TileEntityMekanism, BLOCK extends BlockTypeTile<TILE>> extends BlockTile<TILE, BLOCK> implements IStateFluidLoggable {

        public BlockTileModel(BLOCK type) {
            super(type);
        }

        public BlockTileModel(BLOCK type, AbstractBlock.Properties properties) {
            super(type, properties);
        }
    }
}
