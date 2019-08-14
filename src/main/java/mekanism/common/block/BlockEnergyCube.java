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
import mekanism.common.block.interfaces.ISupportsComparator;
import mekanism.common.block.interfaces.ISupportsRedstone;
import mekanism.common.block.interfaces.ITieredBlock;
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
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * Block class for handling multiple energy cube block IDs. 0: Basic Energy Cube 1: Advanced Energy Cube 2: Elite Energy Cube 3: Ultimate Energy Cube 4: Creative Energy
 * Cube
 *
 * @author AidanBrady
 */
public class BlockEnergyCube extends BlockMekanismContainer implements IHasGui, IStateFacing, ITieredBlock<EnergyCubeTier>, IBlockElectric, IHasInventory, IHasSecurity,
      ISupportsRedstone, IHasTileEntity<TileEntityEnergyCube>, ISupportsComparator {

    private final EnergyCubeTier tier;

    public BlockEnergyCube(EnergyCubeTier tier) {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(2F, 4F));
        this.tier = tier;
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
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        //Charged
        ItemStack charged = new ItemStack(this);
        ((ItemBlockEnergyCube) charged.getItem()).setEnergy(charged, tier.getMaxEnergy());
        items.add(charged);
    }

    @Override
    @Deprecated
    public float getPlayerRelativeBlockHardness(BlockState state, @Nonnull PlayerEntity player, @Nonnull IBlockReader world, @Nonnull BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return SecurityUtils.canAccess(player, tile) ? super.getPlayerRelativeBlockHardness(state, player, world, pos) : 0.0F;
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