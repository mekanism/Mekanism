package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.block.IHasInventory;
import mekanism.api.block.IHasModel;
import mekanism.api.block.IHasTileEntity;
import mekanism.api.block.ISupportsComparator;
import mekanism.api.inventory.AutomationType;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.ITieredBlock;
import mekanism.common.block.states.IStateActive;
import mekanism.common.block.states.IStateFacing;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.base.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockBin extends BlockTileDrops implements IHasModel, IStateFacing, IStateActive, ITieredBlock<BinTier>, IHasTileEntity<TileEntityBin>, ISupportsComparator,
      IHasInventory {

    private final BinTier tier;

    public BlockBin(BinTier tier) {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5F, 10F));
        this.tier = tier;
    }

    @Override
    public BinTier getTier() {
        return tier;
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

    @Override
    public void setTileData(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, TileEntityMekanism tile) {
        if (stack.hasTag() && tile instanceof TileEntityBin) {
            InventoryBin inv = new InventoryBin(stack);
            if (!inv.getItemType().isEmpty()) {
                TileEntityBin bin = (TileEntityBin) tile;
                bin.getBinSlot().setStack(StackUtils.size(inv.getItemType(), inv.getItemCount()));
            }
        }
    }

    @Override
    public void onBlockClicked(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (!world.isRemote) {
            TileEntityBin bin = MekanismUtils.getTileEntity(TileEntityBin.class, world, pos);
            if (bin == null) {
                return;
            }
            BlockRayTraceResult mop = MekanismUtils.rayTrace(world, player);
            //TODO: Check to make sure it wasn't a miss?
            if (mop != null && mop.getFace() == bin.getDirection()) {
                BinInventorySlot binSlot = bin.getBinSlot();
                ItemStack storedStack = binSlot.getStack();
                if (!storedStack.isEmpty()) {
                    ItemStack stack;
                    if (player.isSneaking()) {
                        stack = StackUtils.size(storedStack, 1);
                        if (binSlot.shrinkStack(1, Action.EXECUTE) != 1) {
                            //TODO: Print error that something went wrong??
                        }
                    } else {
                        stack = binSlot.getBottomStack();
                        if (!stack.isEmpty() && binSlot.shrinkStack(stack.getCount(), Action.EXECUTE) != stack.getCount()) {
                            //TODO: Print error that something went wrong??
                        }
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
        TileEntityBin bin = MekanismUtils.getTileEntity(TileEntityBin.class, world, pos);
        if (bin == null) {
            return false;
        }
        if (bin.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return true;
        }
        if (!world.isRemote) {
            BinInventorySlot binSlot = bin.getBinSlot();
            int binMaxSize = binSlot.getLimit(binSlot.getStack());
            if (binSlot.getStack().getCount() < binMaxSize) {
                ItemStack stack = player.getHeldItem(hand);
                if (bin.addTicks == 0) {
                    if (!stack.isEmpty()) {
                        ItemStack remain = binSlot.insertItem(stack, Action.EXECUTE, AutomationType.MANUAL);
                        player.setHeldItem(hand, remain);
                        bin.addTicks = 5;
                    }
                } else if (bin.addTicks > 0 && bin.getItemCount() > 0) {
                    NonNullList<ItemStack> inv = player.inventory.mainInventory;
                    for (int i = 0; i < inv.size(); i++) {
                        if (binSlot.getStack().getCount() == binMaxSize) {
                            break;
                        }
                        ItemStack stackToAdd = inv.get(i);
                        if (!stackToAdd.isEmpty()) {
                            ItemStack remain = binSlot.insertItem(stackToAdd, Action.EXECUTE, AutomationType.MANUAL);
                            inv.set(i, remain);
                            bin.addTicks = 5;
                        }
                        //TODO: Is this needed? Maybe it just updates the hotbar and stuff in which case it is needed
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
                inv.setItemType(bin.getItemType());
            }
        }
        return stack;
    }

    @Override
    public TileEntityType<TileEntityBin> getTileType() {
        switch (tier) {
            case ADVANCED:
                return MekanismTileEntityTypes.ADVANCED_BIN.getTileEntityType();
            case ELITE:
                return MekanismTileEntityTypes.ELITE_BIN.getTileEntityType();
            case ULTIMATE:
                return MekanismTileEntityTypes.ULTIMATE_BIN.getTileEntityType();
            case CREATIVE:
                return MekanismTileEntityTypes.CREATIVE_BIN.getTileEntityType();
            case BASIC:
            default:
                return MekanismTileEntityTypes.BASIC_BIN.getTileEntityType();
        }
    }
}