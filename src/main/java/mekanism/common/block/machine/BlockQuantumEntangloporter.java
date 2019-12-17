package mekanism.common.block.machine;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Upgrade;
import mekanism.api.block.IBlockElectric;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsUpgrades;
import mekanism.common.base.IActiveState;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.block.states.IStateWaterLogged;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.entangloporter.InventoryFrequency;
import mekanism.common.frequency.Frequency;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.QuantumEntangloporterContainer;
import mekanism.common.tile.TileEntityQuantumEntangloporter;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.DirectionProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

//TODO: Evaluate IStateActive here, is used for animateTick. There might be a better way to do this without requiring it to have a state
public class BlockQuantumEntangloporter extends BlockMekanism implements IBlockElectric, IHasGui<TileEntityQuantumEntangloporter>, ISupportsUpgrades, IStateFacing,
      IHasInventory, IHasSecurity, IHasTileEntity<TileEntityQuantumEntangloporter>, IStateActive, IStateWaterLogged {

    //Note: Does not include the "core"
    private static final VoxelShape bounds = VoxelShapeUtils.combine(
          makeCuboidShape(4, 4, 0, 12, 12, 1),//portFront
          makeCuboidShape(0, 4, 4, 1, 12, 12),//portRight
          makeCuboidShape(15, 4, 4, 16, 12, 12),//portLeft
          makeCuboidShape(4, 15, 4, 12, 16, 12),//portTop
          makeCuboidShape(4, 0, 4, 12, 1, 12),//portBottom
          makeCuboidShape(4, 4, 15, 12, 12, 16),//portBack
          makeCuboidShape(13, 13, 0, 16, 16, 3),//corner1
          makeCuboidShape(0, 13, 0, 3, 16, 3),//corner2
          makeCuboidShape(13, 13, 13, 16, 16, 16),//corner3
          makeCuboidShape(0, 13, 13, 3, 16, 16),//corner4
          makeCuboidShape(13, 0, 0, 16, 3, 3),//corner5
          makeCuboidShape(0, 0, 0, 3, 3, 3),//corner6
          makeCuboidShape(13, 0, 13, 16, 3, 16),//corner7
          makeCuboidShape(0, 0, 13, 3, 3, 16),//corner8
          makeCuboidShape(13, 3, 1, 15, 13, 3),//frame1
          makeCuboidShape(1, 3, 1, 3, 13, 3),//frame2
          makeCuboidShape(13, 3, 13, 15, 13, 15),//frame3
          makeCuboidShape(1, 3, 13, 3, 13, 15),//frame4
          makeCuboidShape(3, 1, 1, 13, 3, 3),//frame5
          makeCuboidShape(13, 1, 3, 15, 3, 13),//frame6
          makeCuboidShape(1, 1, 3, 3, 3, 13),//frame7
          makeCuboidShape(3, 1, 13, 13, 3, 15),//frame8
          makeCuboidShape(3, 13, 1, 13, 15, 3),//frame9
          makeCuboidShape(13, 13, 3, 15, 15, 13),//frame10
          makeCuboidShape(1, 13, 3, 3, 15, 13),//frame11
          makeCuboidShape(3, 13, 13, 13, 15, 15),//frame12
          makeCuboidShape(14.5, 3, 0.5, 15.5, 13, 1.5),//frameEdge1
          makeCuboidShape(0.5, 3, 0.5, 1.5, 13, 1.5),//frameEdge2
          makeCuboidShape(14.5, 3, 14.5, 15.5, 13, 15.5),//frameEdge3
          makeCuboidShape(0.5, 3, 14.5, 1.5, 13, 15.5),//frameEdge4
          makeCuboidShape(3, 0.5, 0.5, 13, 1.5, 1.5),//frameEdge5
          makeCuboidShape(14.5, 0.5, 3, 15.5, 1.5, 13),//frameEdge6
          makeCuboidShape(0.5, 0.5, 3, 1.5, 1.5, 13),//frameEdge7
          makeCuboidShape(3, 0.5, 14.5, 13, 1.5, 15.5),//frameEdge8
          makeCuboidShape(3, 14.5, 0.5, 13, 15.5, 1.5),//frameEdge9
          makeCuboidShape(14.5, 14.5, 3, 15.5, 15.5, 13),//frameEdge10
          makeCuboidShape(0.5, 14.5, 3, 1.5, 15.5, 13),//frameEdge11
          makeCuboidShape(3, 14.5, 14.5, 13, 15.5, 15.5)//frameEdge12
    );

    public BlockQuantumEntangloporter() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 16F));
    }

    @Nonnull
    @Override
    public DirectionProperty getFacingProperty() {
        return BlockStateHelper.facingProperty;
    }

    @Override
    public void setTileData(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, @Nonnull TileEntityMekanism tile) {
        if (tile instanceof TileEntityQuantumEntangloporter) {
            if (!world.isRemote && ItemDataUtils.hasData(stack, "entangleporter_frequency")) {
                Frequency.Identity freq = Frequency.Identity.load(ItemDataUtils.getCompound(stack, "entangleporter_frequency"));
                if (freq != null) {
                    ((TileEntityQuantumEntangloporter) tile).setFrequency(freq.name, freq.publicFreq);
                }
            }
        }
    }

    /**
     * @inheritDoc
     * @apiNote Only called on the client side
     */
    @Override
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile != null && MekanismUtils.isActive(world, pos) && ((IActiveState) tile).renderUpdate() && MekanismConfig.client.machineEffects.get()) {
            float xRandom = (float) pos.getX() + 0.5F;
            float yRandom = (float) pos.getY() + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float zRandom = (float) pos.getZ() + 0.5F;
            float iRandom = 0.52F;
            float jRandom = random.nextFloat() * 0.6F - 0.3F;
            Direction side = tile.getDirection();

            switch (side) {
                case WEST:
                    world.addParticle(ParticleTypes.SMOKE, xRandom - iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    world.addParticle(RedstoneParticleData.REDSTONE_DUST, xRandom - iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case EAST:
                    world.addParticle(ParticleTypes.SMOKE, xRandom + iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    world.addParticle(RedstoneParticleData.REDSTONE_DUST, xRandom + iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case NORTH:
                    world.addParticle(ParticleTypes.SMOKE, xRandom + jRandom, yRandom, zRandom - iRandom, 0.0D, 0.0D, 0.0D);
                    world.addParticle(RedstoneParticleData.REDSTONE_DUST, xRandom + jRandom, yRandom, zRandom - iRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case SOUTH:
                    world.addParticle(ParticleTypes.SMOKE, xRandom + jRandom, yRandom, zRandom + iRandom, 0.0D, 0.0D, 0.0D);
                    world.addParticle(RedstoneParticleData.REDSTONE_DUST, xRandom + jRandom, yRandom, zRandom + iRandom, 0.0D, 0.0D, 0.0D);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int getLightValue(BlockState state, ILightReader world, BlockPos pos) {
        if (MekanismConfig.client.enableAmbientLighting.get()) {
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            if (tile instanceof IActiveState && ((IActiveState) tile).lightUpdate() && ((IActiveState) tile).wasActiveRecently()) {
                return MekanismConfig.client.ambientLightingLevel.get();
            }
        }
        return 0;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (world.isRemote) {
            return true;
        }
        TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
        if (tile == null) {
            return false;
        }
        if (tile.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return true;
        }
        return tile.openGui(player);
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(BlockState state, @Nonnull PlayerEntity player, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        return SecurityUtils.canAccess(player, MekanismUtils.getTileEntity(world, pos)) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
    }

    @Override
    public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        //TODO: This is how it was before, but should it be divided by 5 like in Block.java
        return blockResistance;
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
        return bounds;
    }

    @Nonnull
    @Override
    protected ItemStack setItemData(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull TileEntityMekanism tile, @Nonnull ItemStack stack) {
        if (tile instanceof TileEntityQuantumEntangloporter) {
            InventoryFrequency frequency = ((TileEntityQuantumEntangloporter) tile).frequency;
            if (frequency != null) {
                ItemDataUtils.setCompound(stack, "entangleporter_frequency", frequency.getIdentity().serialize());
            }
        }
        return stack;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityQuantumEntangloporter tile) {
        return new ContainerProvider("mekanism.container.quantum_entangloporter", (i, inv, player) -> new QuantumEntangloporterContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityQuantumEntangloporter> getTileType() {
        return MekanismTileEntityTypes.QUANTUM_ENTANGLOPORTER.getTileEntityType();
    }

    @Nonnull
    @Override
    public Set<Upgrade> getSupportedUpgrade() {
        return EnumSet.of(Upgrade.ANCHOR);
    }
}