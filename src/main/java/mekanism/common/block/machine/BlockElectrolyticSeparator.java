package mekanism.common.block.machine;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Upgrade;
import mekanism.api.block.IBlockElectric;
import mekanism.api.block.IBlockSound;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasModel;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.api.block.ISupportsRedstone;
import mekanism.api.block.ISupportsUpgrades;
import mekanism.common.MekanismLang;
import mekanism.common.base.IActiveState;
import mekanism.common.base.ILangEntry;
import mekanism.common.block.BlockMekanism;
import mekanism.common.block.interfaces.IHasDescription;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.block.states.IStateWaterLogged;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.ElectrolyticSeparatorContainer;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.TileEntityElectrolyticSeparator;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.VoxelShapeUtils;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockElectrolyticSeparator extends BlockMekanism implements IBlockElectric, IHasModel, IHasGui<TileEntityElectrolyticSeparator>, ISupportsUpgrades,
      IStateFacing, IStateActive, IHasInventory, IHasSecurity, IHasTileEntity<TileEntityElectrolyticSeparator>, IBlockSound, ISupportsRedstone, ISupportsComparator,
      IStateWaterLogged, IHasDescription {

    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape separator = VoxelShapeUtils.combine(
              makeCuboidShape(0, 0, 0, 16, 4, 16),//base
              makeCuboidShape(15, 3, 3, 16, 13, 13),//portToggle1
              makeCuboidShape(0, 4, 4, 1, 12, 12),//portToggle2a
              makeCuboidShape(4, 4, 0, 12, 12, 1),//portToggle3a
              makeCuboidShape(4, 4, 15, 12, 12, 16),//portToggle4a
              makeCuboidShape(1, 4, 7, 3, 11, 9),//portToggle2b
              makeCuboidShape(7, 4, 1, 8, 11, 3),//portToggle3b
              makeCuboidShape(7, 4, 13, 8, 11, 15),//portToggle4b
              makeCuboidShape(8, 4, 0, 16, 16, 16),//tank1
              makeCuboidShape(0, 4, 9, 7, 14, 16),//tank2
              makeCuboidShape(0, 4, 0, 7, 14, 7),//tank3
              makeCuboidShape(6.5, 10, 7.5, 9.5, 11, 8.5),//tube1
              makeCuboidShape(3, 12, 7.5, 7, 13, 8.5),//tube2
              makeCuboidShape(3, 12, 7.5, 4, 15, 8.5),//tube3
              makeCuboidShape(3, 15, 3, 4, 16, 13),//tube4
              makeCuboidShape(3, 14, 3, 4, 15, 4),//tube5
              makeCuboidShape(3, 14, 12, 4, 15, 13)//tube6
        );
        separator = VoxelShapeUtils.rotate(separator, Rotation.CLOCKWISE_90);
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = VoxelShapeUtils.rotateHorizontal(separator, side);
        }
    }

    public BlockElectrolyticSeparator() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 16F));
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
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        if (MekanismConfig.client.enableAmbientLighting.get()) {
            TileEntity tile = MekanismUtils.getTileEntity(world, pos);
            if (tile instanceof IActiveState && ((IActiveState) tile).lightUpdate() && ((IActiveState) tile).wasActiveRecently()) {
                return MekanismConfig.client.ambientLightingLevel.get();
            }
        }
        return 0;
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
        return bounds[getDirection(state).ordinal() - 2];
    }

    @Override
    public double getUsage() {
        return MekanismConfig.general.FROM_H2.get() * 2;
    }

    @Override
    public double getConfigStorage() {
        return MekanismConfig.storage.electrolyticSeparator.get();
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return MekanismSounds.ELECTROLYTIC_SEPARATOR.getSoundEvent();
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityElectrolyticSeparator tile) {
        return new ContainerProvider(TextComponentUtil.translate(getTranslationKey()), (i, inv, player) -> new ElectrolyticSeparatorContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityElectrolyticSeparator> getTileType() {
        return MekanismTileEntityTypes.ELECTROLYTIC_SEPARATOR.getTileEntityType();
    }

    @Nonnull
    @Override
    public Set<Upgrade> getSupportedUpgrade() {
        return EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING);
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_ELECTROLYTIC_SEPARATOR;
    }
}