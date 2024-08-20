package mekanism.common.block.prefab;

import java.util.function.Function;
import java.util.function.UnaryOperator;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeGui;
import mekanism.common.block.attribute.AttributeParticleFX;
import mekanism.common.block.attribute.AttributeParticleFX.Particle;
import mekanism.common.block.attribute.Attributes.AttributeRedstoneEmitter;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockTile<TILE extends TileEntityMekanism, TYPE extends BlockTypeTile<TILE>> extends BlockBase<TYPE> implements IHasTileEntity<TILE> {

    public BlockTile(TYPE type, UnaryOperator<BlockBehaviour.Properties> propertiesModifier) {
        this(type, propertiesModifier.apply(BlockBehaviour.Properties.of().strength(3.5F, 16).requiresCorrectToolForDrops()));
        //TODO - 1.18: Figure out what the resistance should be (it used to be different in 1.12)
    }

    public BlockTile(TYPE type, BlockBehaviour.Properties properties) {
        super(type, properties);
    }

    @Override
    public TileEntityTypeRegistryObject<TILE> getTileType() {
        return type.getTileType();
    }

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player,
          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            //No tile, we can just skip trying to use without an item
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        } else if (world.isClientSide) {
            return genericClientActivated(stack, tile);
        }
        return tile.tryWrench(state, player, stack).getInteractionResult();
    }

    @NotNull
    @Override
    protected InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return InteractionResult.PASS;
        } else if (world.isClientSide) {
            return Attribute.has(this, AttributeGui.class) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return tile.openGui(player);
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull RandomSource random) {
        super.animateTick(state, world, pos, random);
        if (MekanismConfig.client.machineEffects.get()) {
            AttributeParticleFX particleFX = type.get(AttributeParticleFX.class);
            if (particleFX != null && Attribute.isActive(state)) {
                Direction facing = Attribute.getFacing(state);
                for (Function<RandomSource, Particle> particleFunction : particleFX.getParticleFunctions()) {
                    Particle particle = particleFunction.apply(random);
                    Vec3 particlePos = particle.getPos();
                    if (facing == Direction.WEST) {
                        particlePos = particlePos.yRot(90);
                    } else if (facing == Direction.EAST) {
                        particlePos = particlePos.yRot(270);
                    } else if (facing == Direction.NORTH) {
                        particlePos = particlePos.yRot(180);
                    }
                    particlePos = particlePos.add(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    world.addParticle(particle.getType(), particlePos.x, particlePos.y, particlePos.z, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Block neighborBlock, @NotNull BlockPos neighborPos,
          boolean isMoving) {
        if (!world.isClientSide) {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (tile != null) {
                tile.onNeighborChange(neighborBlock, neighborPos);
            }
        }
    }

    @Override
    protected boolean isSignalSource(@NotNull BlockState state) {
        return type.has(AttributeRedstoneEmitter.class);
    }

    @Override
    public boolean canConnectRedstone(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @Nullable Direction side) {
        return type.has(AttributeRedstoneEmitter.class) || super.canConnectRedstone(state, world, pos, side);
    }

    @Override
    protected int getSignal(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull Direction side) {
        AttributeRedstoneEmitter<TileEntityMekanism> redstoneEmitter = type.get(AttributeRedstoneEmitter.class);
        if (redstoneEmitter != null) {
            TileEntityMekanism tile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
            if (tile != null) {
                return redstoneEmitter.getRedstoneLevel(tile, side.getOpposite());
            }
        }
        return super.getSignal(state, world, pos, side);
    }

    public static class BlockTileModel<TILE extends TileEntityMekanism, BLOCK extends BlockTypeTile<TILE>> extends BlockTile<TILE, BLOCK> implements IStateFluidLoggable {

        public BlockTileModel(BLOCK type, UnaryOperator<BlockBehaviour.Properties> propertiesModifier) {
            super(type, propertiesModifier);
        }

        public BlockTileModel(BLOCK type, BlockBehaviour.Properties properties) {
            super(type, properties);
        }
    }
}
