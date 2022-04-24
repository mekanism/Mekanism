package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.tile.base.WrenchResult;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class BlockBin extends BlockTile<TileEntityBin, BlockTypeTile<TileEntityBin>> {

    public BlockBin(BlockTypeTile<TileEntityBin> type) {
        super(type);
    }

    @Override
    @Deprecated
    public void attack(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Player player) {
        if (!world.isClientSide) {
            TileEntityBin bin = WorldUtils.getTileEntity(TileEntityBin.class, world, pos);
            if (bin != null) {
                BlockHitResult mop = MekanismUtils.rayTrace(player);
                if (mop.getType() != Type.MISS && mop.getDirection() == bin.getDirection()) {
                    BinInventorySlot binSlot = bin.getBinSlot();
                    if (!binSlot.isEmpty()) {
                        ItemStack stack;
                        if (bin.removeTicks == 0) {
                            bin.removeTicks = 3;
                            if (player.isShiftKeyDown()) {
                                stack = StackUtils.size(binSlot.getStack(), 1);
                                MekanismUtils.logMismatchedStackSize(binSlot.shrinkStack(1, Action.EXECUTE), 1);
                            } else {
                                stack = binSlot.getBottomStack();
                                if (!stack.isEmpty()) {
                                    MekanismUtils.logMismatchedStackSize(binSlot.shrinkStack(stack.getCount(), Action.EXECUTE), stack.getCount());
                                }
                            }
                            if (!player.getInventory().add(stack)) {
                                BlockPos dropPos = pos.relative(bin.getDirection());
                                Entity item = new ItemEntity(world, dropPos.getX() + .5f, dropPos.getY() + .3f, dropPos.getZ() + .5f, stack);
                                Vec3 motion = item.getDeltaMovement();
                                item.push(-motion.x(), -motion.y(), -motion.z());
                                world.addFreshEntity(item);
                            } else {
                                world.playSound(null, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                                      0.2F, ((world.random.nextFloat() - world.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                            }
                        }
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand,
          @Nonnull BlockHitResult hit) {
        TileEntityBin bin = WorldUtils.getTileEntity(TileEntityBin.class, world, pos);
        if (bin == null) {
            return InteractionResult.PASS;
        } else if (bin.tryWrench(state, player, hand, hit) != WrenchResult.PASS) {
            return InteractionResult.SUCCESS;
        } else if (!world.isClientSide) {
            BinInventorySlot binSlot = bin.getBinSlot();
            int binMaxSize = binSlot.getLimit(binSlot.getStack());
            if (binSlot.getCount() < binMaxSize) {
                ItemStack stack = player.getItemInHand(hand);
                if (bin.addTicks == 0) {
                    if (!stack.isEmpty()) {
                        ItemStack remain = binSlot.insertItem(stack, Action.EXECUTE, AutomationType.MANUAL);
                        player.setItemInHand(hand, remain);
                        bin.addTicks = 5;
                    }
                } else if (bin.addTicks > 0 && bin.getItemCount() > 0) {
                    NonNullList<ItemStack> inv = player.getInventory().items;
                    for (int i = 0; i < inv.size(); i++) {
                        if (binSlot.getCount() == binMaxSize) {
                            break;
                        }
                        ItemStack stackToAdd = inv.get(i);
                        if (!stackToAdd.isEmpty()) {
                            ItemStack remain = binSlot.insertItem(stackToAdd, Action.EXECUTE, AutomationType.MANUAL);
                            inv.set(i, remain);
                            bin.addTicks = 5;
                        }
                        player.containerMenu.sendAllDataToRemote();
                    }
                }
            }
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }
}