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
import mekanism.common.block.states.IStateFluidLoggable;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.ChemicalCrystallizerContainer;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.TileEntityChemicalCrystallizer;
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
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockChemicalCrystallizer extends BlockMekanism implements IBlockElectric, ISupportsUpgrades, IHasModel, IHasGui<TileEntityChemicalCrystallizer>,
      IStateFacing, IStateActive, IHasInventory, IHasSecurity, IHasTileEntity<TileEntityChemicalCrystallizer>, IBlockSound, ISupportsRedstone, ISupportsComparator,
      IStateFluidLoggable, IHasDescription {

    private static final VoxelShape[] bounds = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        VoxelShape crystallizer = VoxelShapeUtils.combine(
              makeCuboidShape(0, 0, 0, 16, 5, 16),//base
              makeCuboidShape(0, 11, 0, 16, 16, 16),//tank
              makeCuboidShape(3, 4.5, 3, 13, 5.5, 13),//tray
              makeCuboidShape(1, 7, 1, 15, 11, 15),//Shape1
              makeCuboidShape(0, 3, 3, 1, 13, 13),//portRight
              makeCuboidShape(15, 4, 4, 16, 12, 12),//portLeft
              makeCuboidShape(0, 5, 0, 16, 7, 2),//rimBack
              makeCuboidShape(0, 5, 2, 2, 7, 14),//rimRight
              makeCuboidShape(14, 5, 2, 16, 7, 14),//rimLeft
              makeCuboidShape(0, 5, 14, 16, 7, 16),//rimFront
              makeCuboidShape(14.5, 6, 14.5, 15.5, 11, 15.5),//support1
              makeCuboidShape(0.5, 6, 14.5, 1.5, 11, 15.5),//support2
              makeCuboidShape(14.5, 6, 0.5, 15.5, 11, 1.5),//support3
              makeCuboidShape(0.5, 6, 0.5, 1.5, 11, 1.5)//support4
        );
        for (Direction side : EnumUtils.HORIZONTAL_DIRECTIONS) {
            bounds[side.ordinal() - 2] = VoxelShapeUtils.rotateHorizontal(crystallizer, side);
        }
    }

    public BlockChemicalCrystallizer() {
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
        return MekanismConfig.usage.chemicalCrystallizer.get();
    }

    @Override
    public double getConfigStorage() {
        return MekanismConfig.storage.chemicalCrystallizer.get();
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        return MekanismSounds.CHEMICAL_CRYSTALLIZER.getSoundEvent();
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityChemicalCrystallizer tile) {
        return new ContainerProvider(TextComponentUtil.translate(getTranslationKey()), (i, inv, player) -> new ChemicalCrystallizerContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityChemicalCrystallizer> getTileType() {
        return MekanismTileEntityTypes.CHEMICAL_CRYSTALLIZER.getTileEntityType();
    }

    @Nonnull
    @Override
    public Set<Upgrade> getSupportedUpgrade() {
        return EnumSet.of(Upgrade.SPEED, Upgrade.ENERGY, Upgrade.MUFFLING);
    }

    @Nonnull
    @Override
    public ILangEntry getDescription() {
        return MekanismLang.DESCRIPTION_CHEMICAL_CRYSTALLIZER;
    }
}