package mekanism.common.block.machine.factory;

import java.util.Locale;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import mekanism.common.base.FactoryType;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IFactory;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.block.BlockMekanismContainer;
import mekanism.common.block.interfaces.IBlockDisableable;
import mekanism.common.block.interfaces.IBlockElectric;
import mekanism.common.block.interfaces.IBlockSound;
import mekanism.common.block.interfaces.IHasFactoryType;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasSecurity;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ISupportsComparator;
import mekanism.common.block.interfaces.ISupportsRedstone;
import mekanism.common.block.interfaces.ISupportsUpgrades;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.block.machine.BlockChemicalInjectionChamber;
import mekanism.common.block.machine.BlockCombiner;
import mekanism.common.block.machine.BlockCrusher;
import mekanism.common.block.machine.BlockEnergizedSmelter;
import mekanism.common.block.machine.BlockEnrichmentChamber;
import mekanism.common.block.machine.BlockMetallurgicInfuser;
import mekanism.common.block.machine.BlockOsmiumCompressor;
import mekanism.common.block.machine.BlockPrecisionSawmill;
import mekanism.common.block.machine.BlockPurificationChamber;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.FactoryContainer;
import mekanism.common.item.block.machine.factory.ItemBlockFactory;
import mekanism.common.tier.FactoryTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.factory.combining.TileEntityAdvancedCombiningFactory;
import mekanism.common.tile.factory.combining.TileEntityBasicCombiningFactory;
import mekanism.common.tile.factory.combining.TileEntityEliteCombiningFactory;
import mekanism.common.tile.factory.compressing.TileEntityAdvancedCompressingFactory;
import mekanism.common.tile.factory.compressing.TileEntityBasicCompressingFactory;
import mekanism.common.tile.factory.compressing.TileEntityEliteCompressingFactory;
import mekanism.common.tile.factory.crushing.TileEntityAdvancedCrushingFactory;
import mekanism.common.tile.factory.crushing.TileEntityBasicCrushingFactory;
import mekanism.common.tile.factory.crushing.TileEntityEliteCrushingFactory;
import mekanism.common.tile.factory.enriching.TileEntityAdvancedEnrichingFactory;
import mekanism.common.tile.factory.enriching.TileEntityBasicEnrichingFactory;
import mekanism.common.tile.factory.enriching.TileEntityEliteEnrichingFactory;
import mekanism.common.tile.factory.infusing.TileEntityAdvancedInfusingFactory;
import mekanism.common.tile.factory.infusing.TileEntityBasicInfusingFactory;
import mekanism.common.tile.factory.infusing.TileEntityEliteInfusingFactory;
import mekanism.common.tile.factory.injecting.TileEntityAdvancedInjectingFactory;
import mekanism.common.tile.factory.injecting.TileEntityBasicInjectingFactory;
import mekanism.common.tile.factory.injecting.TileEntityEliteInjectingFactory;
import mekanism.common.tile.factory.purifying.TileEntityAdvancedPurifyingFactory;
import mekanism.common.tile.factory.purifying.TileEntityBasicPurifyingFactory;
import mekanism.common.tile.factory.purifying.TileEntityElitePurifyingFactory;
import mekanism.common.tile.factory.sawing.TileEntityAdvancedSawingFactory;
import mekanism.common.tile.factory.sawing.TileEntityBasicSawingFactory;
import mekanism.common.tile.factory.sawing.TileEntityEliteSawingFactory;
import mekanism.common.tile.factory.smelting.TileEntityAdvancedSmeltingFactory;
import mekanism.common.tile.factory.smelting.TileEntityBasicSmeltingFactory;
import mekanism.common.tile.factory.smelting.TileEntityEliteSmeltingFactory;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

public class BlockFactory extends BlockMekanismContainer implements IBlockElectric, ISupportsUpgrades, IHasGui<TileEntityFactory>, IStateFacing, IStateActive, IBlockSound,
      ITieredBlock<FactoryTier>, IHasFactoryType, IHasInventory, IHasSecurity, IHasTileEntity<TileEntityFactory>, ISupportsRedstone, IBlockDisableable,
      ISupportsComparator {

    private BooleanValue enabledReference;

    private final FactoryTier tier;
    private final FactoryType type;

    public BlockFactory(@Nonnull FactoryTier tier, @Nonnull FactoryType type) {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(3.5F, 16F));
        this.tier = tier;
        this.type = type;
        String name = tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_" + type.getRegistryNameComponent() + "_factory";
        setRegistryName(new ResourceLocation(Mekanism.MODID, name));
    }

    @Override
    public FactoryTier getTier() {
        return tier;
    }

    @Nonnull
    @Override
    public FactoryType getFactoryType() {
        return type;
    }

    @Override
    public void setTileData(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, @Nonnull TileEntityMekanism tile) {
        if (tile instanceof TileEntityFactory) {
            RecipeType recipeType = ((ItemBlockFactory) stack.getItem()).getRecipeTypeOrNull(stack);
            if (recipeType != null) {
                ((TileEntityFactory) tile).setRecipeType(recipeType);
            }
            world.notifyNeighborsOfStateChange(pos, tile.getBlockType());
            Mekanism.packetHandler.sendUpdatePacket(tile);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
        TileEntityMekanism tileEntity = (TileEntityMekanism) world.getTileEntity(pos);
        if (MekanismUtils.isActive(world, pos) && ((IActiveState) tileEntity).renderUpdate() && MekanismConfig.client.machineEffects.get()) {
            float xRandom = (float) pos.getX() + 0.5F;
            float yRandom = (float) pos.getY() + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float zRandom = (float) pos.getZ() + 0.5F;
            float iRandom = 0.52F;
            float jRandom = random.nextFloat() * 0.6F - 0.3F;
            Direction side = tileEntity.getDirection();

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
    public int getLightValue(BlockState state, IEnviromentBlockReader world, BlockPos pos) {
        if (MekanismConfig.client.enableAmbientLighting.get()) {
            TileEntity tileEntity = MekanismUtils.getTileEntitySafe(world, pos);
            if (tileEntity instanceof IActiveState && ((IActiveState) tileEntity).lightUpdate() && ((IActiveState) tileEntity).wasActiveRecently()) {
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
        TileEntityMekanism tileEntity = (TileEntityMekanism) world.getTileEntity(pos);
        if (tileEntity.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return true;
        }
        if (tileEntity.openGui(player)) {
            return true;
        }
        return false;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        //TODO
        switch (type) {
            case SMELTING:
                switch (tier) {
                    case BASIC:
                        return new TileEntityBasicSmeltingFactory();
                    case ADVANCED:
                        return new TileEntityAdvancedSmeltingFactory();
                    case ELITE:
                        return new TileEntityEliteSmeltingFactory();
                }
            case ENRICHING:
                switch (tier) {
                    case BASIC:
                        return new TileEntityBasicEnrichingFactory();
                    case ADVANCED:
                        return new TileEntityAdvancedEnrichingFactory();
                    case ELITE:
                        return new TileEntityEliteEnrichingFactory();
                }
            case CRUSHING:
                switch (tier) {
                    case BASIC:
                        return new TileEntityBasicCrushingFactory();
                    case ADVANCED:
                        return new TileEntityAdvancedCrushingFactory();
                    case ELITE:
                        return new TileEntityEliteCrushingFactory();
                }
            case COMPRESSING:
                switch (tier) {
                    case BASIC:
                        return new TileEntityBasicCompressingFactory();
                    case ADVANCED:
                        return new TileEntityAdvancedCompressingFactory();
                    case ELITE:
                        return new TileEntityEliteCompressingFactory();
                }
            case COMBINING:
                switch (tier) {
                    case BASIC:
                        return new TileEntityBasicCombiningFactory();
                    case ADVANCED:
                        return new TileEntityAdvancedCombiningFactory();
                    case ELITE:
                        return new TileEntityEliteCombiningFactory();
                }
            case PURIFYING:
                switch (tier) {
                    case BASIC:
                        return new TileEntityBasicPurifyingFactory();
                    case ADVANCED:
                        return new TileEntityAdvancedPurifyingFactory();
                    case ELITE:
                        return new TileEntityElitePurifyingFactory();
                }
            case INJECTING:
                switch (tier) {
                    case BASIC:
                        return new TileEntityBasicInjectingFactory();
                    case ADVANCED:
                        return new TileEntityAdvancedInjectingFactory();
                    case ELITE:
                        return new TileEntityEliteInjectingFactory();
                }
            case INFUSING:
                switch (tier) {
                    case BASIC:
                        return new TileEntityBasicInfusingFactory();
                    case ADVANCED:
                        return new TileEntityAdvancedInfusingFactory();
                    case ELITE:
                        return new TileEntityEliteInfusingFactory();
                }
            case SAWING:
                switch (tier) {
                    case BASIC:
                        return new TileEntityBasicSawingFactory();
                    case ADVANCED:
                        return new TileEntityAdvancedSawingFactory();
                    case ELITE:
                        return new TileEntityEliteSawingFactory();
                }
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(BlockState state, @Nonnull PlayerEntity player, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return SecurityUtils.canAccess(player, tile) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
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
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Nonnull
    @Override
    protected ItemStack setItemData(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull TileEntityMekanism tile, @Nonnull ItemStack stack) {
        if (tile instanceof TileEntityFactory) {
            ((IFactory) stack.getItem()).setRecipeType(((TileEntityFactory) tile).getRecipeType().ordinal(), stack);
        }
        return stack;
    }

    @Override
    public int getInventorySize() {
        return 5 + tier.processes * 2;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityFactory> getTileClass() {
        //TODO: Clean this up
        switch (type) {
            case SMELTING:
                switch (tier) {
                    case BASIC:
                        return TileEntityBasicSmeltingFactory.class;
                    case ADVANCED:
                        return TileEntityAdvancedSmeltingFactory.class;
                    case ELITE:
                        return TileEntityEliteSmeltingFactory.class;
                }
            case ENRICHING:
                switch (tier) {
                    case BASIC:
                        return TileEntityBasicEnrichingFactory.class;
                    case ADVANCED:
                        return TileEntityAdvancedEnrichingFactory.class;
                    case ELITE:
                        return TileEntityEliteEnrichingFactory.class;
                }
            case CRUSHING:
                switch (tier) {
                    case BASIC:
                        return TileEntityBasicCrushingFactory.class;
                    case ADVANCED:
                        return TileEntityAdvancedCrushingFactory.class;
                    case ELITE:
                        return TileEntityEliteCrushingFactory.class;
                }
            case COMPRESSING:
                switch (tier) {
                    case BASIC:
                        return TileEntityBasicCompressingFactory.class;
                    case ADVANCED:
                        return TileEntityAdvancedCompressingFactory.class;
                    case ELITE:
                        return TileEntityEliteCompressingFactory.class;
                }
            case COMBINING:
                switch (tier) {
                    case BASIC:
                        return TileEntityBasicCombiningFactory.class;
                    case ADVANCED:
                        return TileEntityAdvancedCombiningFactory.class;
                    case ELITE:
                        return TileEntityEliteCombiningFactory.class;
                }
            case PURIFYING:
                switch (tier) {
                    case BASIC:
                        return TileEntityBasicPurifyingFactory.class;
                    case ADVANCED:
                        return TileEntityAdvancedPurifyingFactory.class;
                    case ELITE:
                        return TileEntityElitePurifyingFactory.class;
                }
            case INJECTING:
                switch (tier) {
                    case BASIC:
                        return TileEntityBasicInjectingFactory.class;
                    case ADVANCED:
                        return TileEntityAdvancedInjectingFactory.class;
                    case ELITE:
                        return TileEntityEliteInjectingFactory.class;
                }
            case INFUSING:
                switch (tier) {
                    case BASIC:
                        return TileEntityBasicInfusingFactory.class;
                    case ADVANCED:
                        return TileEntityAdvancedInfusingFactory.class;
                    case ELITE:
                        return TileEntityEliteInfusingFactory.class;
                }
            case SAWING:
                switch (tier) {
                    case BASIC:
                        return TileEntityBasicSawingFactory.class;
                    case ADVANCED:
                        return TileEntityAdvancedSawingFactory.class;
                    case ELITE:
                        return TileEntityEliteSawingFactory.class;
                }
        }
        return null;
    }

    @Nonnull
    @Override
    public SoundEvent getSoundEvent() {
        switch (type) {
            case ENRICHING:
                return BlockEnrichmentChamber.SOUND_EVENT;
            case CRUSHING:
                return BlockCrusher.SOUND_EVENT;
            case COMPRESSING:
                return BlockOsmiumCompressor.SOUND_EVENT;
            case COMBINING:
                return BlockCombiner.SOUND_EVENT;
            case PURIFYING:
                return BlockPurificationChamber.SOUND_EVENT;
            case INJECTING:
                return BlockChemicalInjectionChamber.SOUND_EVENT;
            case INFUSING:
                return BlockMetallurgicInfuser.SOUND_EVENT;
            case SAWING:
                return BlockPrecisionSawmill.SOUND_EVENT;
            case SMELTING:
            default:
                return BlockEnergizedSmelter.SOUND_EVENT;
        }
    }

    @Override
    public boolean isEnabled() {
        return enabledReference == null ? true : enabledReference.get();
    }

    @Override
    public void setEnabledConfigReference(BooleanValue enabledReference) {
        this.enabledReference = enabledReference;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityFactory tile) {
        return new ContainerProvider("mekanism.container.factory", (i, inv, player) -> new FactoryContainer(i, inv, tile));
    }
}