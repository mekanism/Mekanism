package mekanism.common.block.machine.factory;

import java.util.Locale;
import java.util.Random;
import javax.annotation.Nonnull;
import mekanism.common.Mekanism;
import mekanism.common.base.FactoryType;
import mekanism.common.base.IActiveState;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFactory;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.block.BlockMekanismContainer;
import mekanism.common.block.interfaces.IBlockElectric;
import mekanism.common.block.interfaces.IHasFactoryType;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasSecurity;
import mekanism.common.block.interfaces.ISupportsUpgrades;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.config.MekanismConfig;
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
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockFactory extends BlockMekanismContainer implements IBlockElectric, ISupportsUpgrades, IHasGui, IStateFacing, IStateActive,
      ITieredBlock<FactoryTier>, IHasFactoryType, IHasInventory, IHasSecurity {

    private final FactoryTier tier;
    private final FactoryType type;

    public BlockFactory(@Nonnull FactoryTier tier, @Nonnull FactoryType type) {
        super(Material.IRON);
        this.tier = tier;
        this.type = type;
        setHardness(3.5F);
        setResistance(16F);
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

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return BlockStateHelper.getBlockState(this);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        //TODO
        return 0;
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        return BlockStateHelper.getActualState(this, state, MekanismUtils.getTileEntitySafe(world, pos));
    }

    @Override
    public void setTileData(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack, @Nonnull TileEntityMekanism tile) {
        if (tile instanceof TileEntityFactory) {
            RecipeType recipeType = ((ItemBlockFactory) stack.getItem()).getRecipeTypeOrNull(stack);
            if (recipeType != null) {
                ((TileEntityFactory) tile).setRecipeType(recipeType);
            }
            world.notifyNeighborsOfStateChange(pos, tile.getBlockType(), true);
            Mekanism.packetHandler.sendUpdatePacket(tile);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        TileEntityMekanism tileEntity = (TileEntityMekanism) world.getTileEntity(pos);
        if (MekanismUtils.isActive(world, pos) && ((IActiveState) tileEntity).renderUpdate() && MekanismConfig.current().client.machineEffects.val()) {
            float xRandom = (float) pos.getX() + 0.5F;
            float yRandom = (float) pos.getY() + 0.0F + random.nextFloat() * 6.0F / 16.0F;
            float zRandom = (float) pos.getZ() + 0.5F;
            float iRandom = 0.52F;
            float jRandom = random.nextFloat() * 0.6F - 0.3F;
            EnumFacing side = tileEntity.getDirection();

            switch (side) {
                case WEST:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, xRandom - iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, xRandom - iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case EAST:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, xRandom + iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, xRandom + iRandom, yRandom, zRandom + jRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case NORTH:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, xRandom + jRandom, yRandom, zRandom - iRandom, 0.0D, 0.0D, 0.0D);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, xRandom + jRandom, yRandom, zRandom - iRandom, 0.0D, 0.0D, 0.0D);
                    break;
                case SOUTH:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, xRandom + jRandom, yRandom, zRandom + iRandom, 0.0D, 0.0D, 0.0D);
                    world.spawnParticle(EnumParticleTypes.REDSTONE, xRandom + jRandom, yRandom, zRandom + iRandom, 0.0D, 0.0D, 0.0D);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (MekanismConfig.current().client.enableAmbientLighting.val()) {
            TileEntity tileEntity = MekanismUtils.getTileEntitySafe(world, pos);
            if (tileEntity instanceof IActiveState && ((IActiveState) tileEntity).lightUpdate() && ((IActiveState) tileEntity).wasActiveRecently()) {
                return MekanismConfig.current().client.ambientLightingLevel.val();
            }
        }
        return 0;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }
        TileEntityMekanism tileEntity = (TileEntityMekanism) world.getTileEntity(pos);
        if (tileEntity.tryWrench(state, player, hand, () -> new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos)) != WrenchResult.PASS) {
            return true;
        }
        if (tileEntity.openGui(player)) {
            return true;
        }
        return false;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
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

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Nonnull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(IBlockState state, @Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return SecurityUtils.canAccess(player, tile) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        //TODO: This is how it was before, but should it be divided by 5 like in Block.java
        return blockResistance;
    }

    @Override
    @Deprecated
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    @Deprecated
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IComparatorSupport) {
            return ((IComparatorSupport) tileEntity).getRedstoneLevel();
        }
        return 0;
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Nonnull
    @Override
    protected ItemStack setItemData(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull TileEntityMekanism tile, @Nonnull ItemStack stack) {
        if (tile instanceof TileEntityFactory) {
            ((IFactory) stack.getItem()).setRecipeType(((TileEntityFactory) tile).getRecipeType().ordinal(), stack);
        }
        return stack;
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public int getGuiID() {
        return 11;
    }

    @Override
    public int getInventorySize() {
        return 5 + tier.processes * 2;
    }
}