package mekanism.common.block;

import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.api.block.IBlockElectric;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasSecurity;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.api.block.ISupportsRedstone;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IHasGui;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.tile.energy.EnergyCubeContainer;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tile.TileEntityEnergyCube;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.SecurityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Block class for handling multiple energy cube block IDs. 0: Basic Energy Cube 1: Advanced Energy Cube 2: Elite Energy Cube 3: Ultimate Energy Cube 4: Creative Energy
 * Cube
 *
 * @author AidanBrady
 */
public class BlockEnergyCube extends BlockMekanismContainer implements IHasGui<TileEntityEnergyCube>, IStateFacing, ITieredBlock<EnergyCubeTier>, IBlockElectric, IHasInventory, IHasSecurity,
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

    @Nonnull
    @Override
    public DirectionProperty getFacingProperty() {
        return BlockStateHelper.facingProperty;
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
        if (tileEntity == null) {
            return false;
        }
        if (tileEntity.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return true;
        }
        return tileEntity.openGui(player);
    }

    @Override
    public double getStorage() {
        return tier.getMaxEnergy();
    }

    @Override
    public int getInventorySize() {
        return 2;
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public INamedContainerProvider getProvider(TileEntityEnergyCube tile) {
        return new ContainerProvider("mekanism.container.energy_cube", (i, inv, player) -> new EnergyCubeContainer(i, inv, tile));
    }

    @Override
    public TileEntityType<TileEntityEnergyCube> getTileType() {
        switch (tier) {
            case ADVANCED:
                return MekanismTileEntityTypes.ADVANCED_ENERGY_CUBE;
            case ELITE:
                return MekanismTileEntityTypes.ELITE_ENERGY_CUBE;
            case ULTIMATE:
                return MekanismTileEntityTypes.ULTIMATE_ENERGY_CUBE;
            case CREATIVE:
                return MekanismTileEntityTypes.CREATIVE_ENERGY_CUBE;
            case BASIC:
            default:
                return MekanismTileEntityTypes.BASIC_ENERGY_CUBE;
        }
    }
}