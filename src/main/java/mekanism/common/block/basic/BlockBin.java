package mekanism.common.block.basic;

import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.base.IActiveState;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.IHasModel;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.tile.bin.TileEntityAdvancedBin;
import mekanism.common.tile.bin.TileEntityBasicBin;
import mekanism.common.tile.bin.TileEntityBin;
import mekanism.common.tile.bin.TileEntityCreativeBin;
import mekanism.common.tile.bin.TileEntityEliteBin;
import mekanism.common.tile.bin.TileEntityUltimateBin;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockBin extends BlockTileDrops implements IHasModel, IStateFacing, IStateActive, ITieredBlock<BinTier>, IHasTileEntity<TileEntityBin> {

    private final BinTier tier;

    public BlockBin(BinTier tier) {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5F, 10F));
        this.tier = tier;
        setRegistryName(new ResourceLocation(Mekanism.MODID, tier.getBaseTier().getSimpleName().toLowerCase(Locale.ROOT) + "_bin"));
    }

    @Override
    public BinTier getTier() {
        return tier;
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
    public BlockState getActualState(@Nonnull BlockState state, IBlockReader world, BlockPos pos) {
        return BlockStateHelper.getActualState(this, state, MekanismUtils.getTileEntitySafe(world, pos));
    }

    @Override
    @Deprecated
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        if (!world.isRemote) {
            TileEntity tileEntity = new Coord4D(pos, world).getTileEntity(world);
            if (tileEntity instanceof TileEntityMekanism) {
                ((TileEntityMekanism) tileEntity).onNeighborChange(neighborBlock);
            }
        }
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        TileEntity tileEntity = MekanismUtils.getTileEntitySafe(world, pos);
        if (tileEntity instanceof IActiveState) {
            if (((IActiveState) tileEntity).getActive() && ((IActiveState) tileEntity).lightUpdate()) {
                return 15;
            }
        }
        return super.getLightValue(state, world, pos);
    }

    @Override
    public void setTileData(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, TileEntityMekanism tile) {
        if (stack.hasTag() && tile instanceof TileEntityBin) {
            InventoryBin inv = new InventoryBin(stack);
            if (!inv.getItemType().isEmpty()) {
                TileEntityBin bin = (TileEntityBin) tile;
                bin.setItemType(inv.getItemType());
                bin.setItemCount(inv.getItemCount());
            }
        }
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, PlayerEntity player) {
        if (!world.isRemote) {
            TileEntityBin bin = (TileEntityBin) world.getTileEntity(pos);
            BlockRayTraceResult mop = MekanismUtils.rayTrace(world, player);

            if (mop != null && mop.getFace() == bin.getDirection()) {
                if (!bin.bottomStack.isEmpty()) {
                    ItemStack stack;
                    if (player.isSneaking()) {
                        stack = bin.remove(1).copy();
                    } else {
                        stack = bin.removeStack().copy();
                    }
                    if (!player.inventory.addItemStackToInventory(stack)) {
                        BlockPos dropPos = pos.offset(bin.getDirection());
                        Entity item = new ItemEntity(world, dropPos.getX() + .5f, dropPos.getY() + .3f, dropPos.getZ() + .5f, stack);
                        Vec3d motion = item.getMotion();
                        item.addVelocity(-motion.getX(), -motion.getY(), -motion.getZ());
                        world.addEntity(item);
                    } else {
                        world.playSound(null, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS,
                              0.2F, ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    }
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        ItemStack stack = player.getHeldItem(hand);
        TileEntityBin bin = (TileEntityBin) world.getTileEntity(pos);
        if (bin.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return true;
        }
        if (!world.isRemote) {
            if (bin.getItemCount() < bin.tier.getStorage()) {
                if (bin.addTicks == 0) {
                    if (!stack.isEmpty()) {
                        ItemStack remain = bin.add(stack);
                        player.setHeldItem(hand, remain);
                        bin.addTicks = 5;
                    }
                } else if (bin.addTicks > 0 && bin.getItemCount() > 0) {
                    NonNullList<ItemStack> inv = player.inventory.mainInventory;
                    for (int i = 0; i < inv.size(); i++) {
                        if (bin.getItemCount() == bin.tier.getStorage()) {
                            break;
                        }
                        if (!inv.get(i).isEmpty()) {
                            ItemStack remain = bin.add(inv.get(i));
                            inv.set(i, remain);
                            bin.addTicks = 5;
                        }
                        ((ServerPlayerEntity) player).sendContainerToPlayer(player.openContainer);
                    }
                }
            }
        }
        return true;
    }

    @Nonnull
    @Override
    protected ItemStack setItemData(@Nonnull BlockState state, @Nonnull IBlockReader world, @Nonnull BlockPos pos, @Nonnull TileEntityMekanism tile, @Nonnull ItemStack stack) {
        if (tile instanceof TileEntityBin) {
            TileEntityBin bin = (TileEntityBin) tile;
            if (bin.getItemCount() > 0) {
                InventoryBin inv = new InventoryBin(stack);
                inv.setItemCount(bin.getItemCount());
                inv.setItemType(bin.itemType);
            }
        }
        return stack;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        switch (tier) {
            case BASIC:
                return new TileEntityBasicBin();
            case ADVANCED:
                return new TileEntityAdvancedBin();
            case ELITE:
                return new TileEntityEliteBin();
            case ULTIMATE:
                return new TileEntityUltimateBin();
            case CREATIVE:
                return new TileEntityCreativeBin();
        }
        return null;
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState blockState) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
        return ((TileEntityBin) world.getTileEntity(pos)).getRedstoneLevel();
    }

    @Override
    public int getLightOpacity(BlockState state, IBlockReader world, BlockPos pos) {
        return 0;
    }

    @Nullable
    @Override
    public Class<? extends TileEntityBin> getTileClass() {
        switch (tier) {
            case BASIC:
                return TileEntityBasicBin.class;
            case ADVANCED:
                return TileEntityAdvancedBin.class;
            case ELITE:
                return TileEntityEliteBin.class;
            case ULTIMATE:
                return TileEntityUltimateBin.class;
            case CREATIVE:
                return TileEntityCreativeBin.class;
        }
        return null;
    }
}