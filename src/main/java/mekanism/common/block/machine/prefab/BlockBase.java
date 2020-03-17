package mekanism.common.block.machine.prefab;

import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.block.IHasTileEntity;
import mekanism.common.base.IActiveState;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeCustomShape;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeParticleFX.Particle;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.block.attribute.AttributeStateFacing;
import mekanism.common.block.attribute.Attributes.AttributeCustomResistance;
import mekanism.common.block.attribute.Attributes.AttributeRedstoneEmitter;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.ITypeBlock;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.BlockTile;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockBase<TILE extends TileEntityMekanism, TYPE extends BlockTile<TILE>> extends BlockMekanism implements IHasTileEntity<TILE>, IHasDescription, ITypeBlock<TILE> {

    protected TYPE type;

    public BlockBase(TYPE type) {
        this(type, Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 16F));
    }

    public BlockBase(TYPE type, Block.Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public BlockTile<TILE> getType() {
        return type;
    }

    @Override
    public TileEntityType<TILE> getTileType() {
        return type.getTileType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return type.getDescription();
    }

    @Nonnull
    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return ActionResultType.PASS;
        }
        if (tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return ActionResultType.SUCCESS;
        }
        return type.has(AttributeGui.class) ? tile.openGui(player) : ActionResultType.PASS;
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        if (MekanismConfig.client.enableAmbientLighting.get() && type.has(AttributeStateActive.class)) {
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            if (tile instanceof IActiveState && ((IActiveState) tile).lightUpdate() && ((IActiveState) tile).getActive()) {
                return MekanismConfig.client.ambientLightingLevel.get();
            }
        }
        return 0;
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(BlockState state, @Nonnull PlayerEntity player, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return SecurityUtils.canAccess(player, MekanismUtils.getTileEntity(world, pos)) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
    }

    @Override
    public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        return type.has(AttributeCustomResistance.class) ? type.get(AttributeCustomResistance.class).getResistance() : blockResistance;
    }

    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile != null && MekanismUtils.isActive(world, pos) && Attribute.has(state.getBlock(), AttributeParticleFX.class)) {
            for (Function<Random, Particle> particleFunction : type.get(AttributeParticleFX.class).getParticleFunctions()) {
                Particle particle = particleFunction.apply(random);
                Vec3d particlePos = particle.getPos();
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

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        if (type.has(AttributeCustomShape.class)) {
            AttributeStateFacing attr = type.get(AttributeStateFacing.class);
            return type.get(AttributeCustomShape.class).getBounds()[attr.getDirection(state).ordinal() - (attr.getFacingProperty() == BlockStateHelper.facingProperty ? 0 : 2)];
        }
        return super.getShape(state, world, pos, context);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return type.has(AttributeRedstoneEmitter.class) ? true : super.canConnectRedstone(state, world, pos, side);
    }

    @Override
    @Deprecated
    public int getWeakPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        if (type.has(AttributeRedstoneEmitter.class)) {
            TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (tile != null) {
                return tile.getRedstoneLevel();
            }
        }
        return 0;
    }

    public static class BlockMachineModel<TILE extends TileEntityMekanism, MACHINE extends BlockTile<TILE>> extends BlockBase<TILE, MACHINE> implements IStateFluidLoggable {

        public BlockMachineModel(MACHINE machineType) {
            super(machineType);
        }
    }
}
