package mekanism.common.block;

import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IBlockElectric;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.IHasInventory;
import mekanism.common.block.interfaces.IHasSecurity;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ISupportsRedstone;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.tile.energy_cube.TileEntityAdvancedEnergyCube;
import mekanism.common.tile.energy_cube.TileEntityBasicEnergyCube;
import mekanism.common.tile.energy_cube.TileEntityCreativeEnergyCube;
import mekanism.common.tile.energy_cube.TileEntityEliteEnergyCube;
import mekanism.common.tile.energy_cube.TileEntityEnergyCube;
import mekanism.common.tile.energy_cube.TileEntityUltimateEnergyCube;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

/**
 * Block class for handling multiple energy cube block IDs. 0: Basic Energy Cube 1: Advanced Energy Cube 2: Elite Energy Cube 3: Ultimate Energy Cube 4: Creative Energy
 * Cube
 *
 * @author AidanBrady
 */
public class BlockEnergyCube extends BlockMekanismContainer implements IHasGui, IStateFacing, ITieredBlock<EnergyCubeTier>, IBlockElectric, IHasInventory, IHasSecurity,
      ISupportsRedstone, IHasTileEntity<TileEntityEnergyCube> {

    private final EnergyCubeTier tier;

    public BlockEnergyCube(EnergyCubeTier tier) {
        super(Material.IRON);
        this.tier = tier;
        setHardness(2F);
        setResistance(4F);
        setRegistryName(new ResourceLocation(Mekanism.MODID, tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_energy_cube"));
    }

    @Override
    public EnergyCubeTier getTier() {
        return tier;
    }

    @Override
    public boolean supportsAll() {
        return true;
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return BlockStateHelper.getBlockState(this);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        //TODO
        return 0;
    }

    @Nonnull
    @Override
    @Deprecated
    public BlockState getActualState(@Nonnull BlockState state, IWorldReader world, BlockPos pos) {
        return BlockStateHelper.getActualState(this, state, MekanismUtils.getTileEntitySafe(world, pos));
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    public void setTileData(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, @Nonnull TileEntityMekanism tile) {
        if (tile instanceof TileEntityEnergyCube) {
            TileEntityEnergyCube cube = (TileEntityEnergyCube) tile;
            if (cube.tier == EnergyCubeTier.CREATIVE) {
                cube.configComponent.fillConfig(TransmissionType.ENERGY, ((ItemBlockEnergyCube) stack.getItem()).getEnergy(stack) > 0 ? 2 : 1);
            }
        }
    }

    @Override
    public void getSubBlocks(ItemGroup creativetabs, NonNullList<ItemStack> list) {
        //Empty
        list.add(new ItemStack(this));
        //Charged
        ItemStack charged = new ItemStack(this);
        ((ItemBlockEnergyCube) charged.getItem()).setEnergy(charged, tier.getMaxEnergy());
        list.add(charged);
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(BlockState state, @Nonnull PlayerEntity player, @Nonnull World world, @Nonnull BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return SecurityUtils.canAccess(player, tile) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        //TODO: Put the wrench logic in TileEntityMekanism??
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
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull BlockState state) {
        switch (tier) {
            case BASIC:
                return new TileEntityBasicEnergyCube();
            case ADVANCED:
                return new TileEntityAdvancedEnergyCube();
            case ELITE:
                return new TileEntityEliteEnergyCube();
            case ULTIMATE:
                return new TileEntityUltimateEnergyCube();
            case CREATIVE:
                return new TileEntityCreativeEnergyCube();
        }
        return null;
    }

    @Override
    @Deprecated
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @Override
    @Deprecated
    public int getComparatorInputOverride(BlockState state, World world, BlockPos pos) {
        return ((TileEntityEnergyCube) world.getTileEntity(pos)).getRedstoneLevel();
    }

    @Override
    public int getGuiID() {
        return 8;
    }

    @Override
    public double getStorage() {
        return tier.getMaxEnergy();
    }

    @Override
    public int getInventorySize() {
        return 2;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityEnergyCube> getTileClass() {
        switch (tier) {
            case BASIC:
                return TileEntityBasicEnergyCube.class;
            case ADVANCED:
                return TileEntityAdvancedEnergyCube.class;
            case ELITE:
                return TileEntityEliteEnergyCube.class;
            case ULTIMATE:
                return TileEntityUltimateEnergyCube.class;
            case CREATIVE:
                return TileEntityCreativeEnergyCube.class;
        }
        return null;
    }
}