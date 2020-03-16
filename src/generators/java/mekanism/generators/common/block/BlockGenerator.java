package mekanism.generators.common.block;

import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nonnull;
import mekanism.api.block.IBlockElectric;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.api.block.ISupportsRedstone;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeCustomShape;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeParticleFX.Particle;
import mekanism.common.block.attribute.AttributeStateActive;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.generators.common.content.blocktype.Generator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockGenerator<TILE extends TileEntityMekanism> extends BlockMekanism implements IBlockElectric, IStateFacing, IHasInventory, IHasSecurity,
    IHasTileEntity<TILE>, ISupportsComparator, ISupportsRedstone, IStateFluidLoggable, AttributeStateActive, IHasDescription {

    protected Generator<TILE> generatorType;

    public BlockGenerator(Generator<TILE> generatorType) {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 8F));
        this.generatorType = generatorType;
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

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(BlockState state, @Nonnull PlayerEntity player, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return SecurityUtils.canAccess(player, MekanismUtils.getTileEntity(world, pos)) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
    }

    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile != null && MekanismUtils.isActive(world, pos) && Attribute.has(state.getBlock(), AttributeParticleFX.class)) {
            for (Function<Random, Particle> particleFunction : generatorType.get(AttributeParticleFX.class).getParticleFunctions()) {
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
        return tile.openGui(player);
    }

    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return generatorType.has(AttributeCustomShape.class) ? generatorType.get(AttributeCustomShape.class).getBounds()[getDirection(state).ordinal() - 2] : super.getShape(state, world, pos, context);
    }

    @Override
    public double getStorage() {
        return generatorType.getConfigStorage();
    }

    @Override
    public TileEntityType<TILE> getTileType() {
        return generatorType.getTileType();
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return generatorType.getDescription();
    }
}
