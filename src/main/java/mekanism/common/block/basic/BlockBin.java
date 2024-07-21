package mekanism.common.block.basic;

import java.util.function.UnaryOperator;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class BlockBin extends BlockTile<TileEntityBin, BlockTypeTile<TileEntityBin>> {

    public BlockBin(BlockTypeTile<TileEntityBin> type, UnaryOperator<BlockBehaviour.Properties> propertiesModifier) {
        super(type, propertiesModifier);
    }

    @Override
    protected void attack(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player) {
        if (!world.isClientSide) {
            TileEntityBin bin = WorldUtils.getTileEntity(TileEntityBin.class, world, pos);
            if (bin != null) {
                BlockHitResult mop = MekanismUtils.rayTrace(player);
                if (mop.getType() != Type.MISS && mop.getDirection() == bin.getDirection()) {
                    BinInventorySlot binSlot = bin.getBinSlot();
                    if (!binSlot.isEmpty() && bin.removeTicks == 0) {
                        bin.removeTicks = 3;
                        ItemStack stack;
                        if (player.isShiftKeyDown()) {
                            stack = binSlot.getBottomStack();
                            if (!stack.isEmpty()) {
                                MekanismUtils.logMismatchedStackSize(binSlot.shrinkStack(stack.getCount(), Action.EXECUTE), stack.getCount());
                            }
                        } else {
                            stack = binSlot.getStack().copyWithCount(1);
                            MekanismUtils.logMismatchedStackSize(binSlot.shrinkStack(1, Action.EXECUTE), 1);
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

    @NotNull
    @Override
    protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player,
          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        TileEntityBin bin = WorldUtils.getTileEntity(TileEntityBin.class, world, pos);
        if (bin == null) {
            //No tile, we can just skip trying to use without an item
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        ItemInteractionResult wrenchResult = bin.tryWrench(state, player, stack).getInteractionResult();
        if (wrenchResult.result() != InteractionResult.PASS) {
            return wrenchResult;
        } else if (hit.getDirection() != bin.getDirection()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.isEmpty() && player.isShiftKeyDown()) {
            return bin.toggleLock() ? ItemInteractionResult.sidedSuccess(world.isClientSide) : ItemInteractionResult.FAIL;
        } else if (!world.isClientSide) {
            BinInventorySlot binSlot = bin.getBinSlot();
            ItemStack storedStack = binSlot.isLocked() ? binSlot.getLockStack() : binSlot.getStack();
            int binMaxSize = binSlot.getLimit(storedStack);
            if (binSlot.getCount() < binMaxSize) {
                //TODO - 1.21: Make add ticks and removeTicks functional somehow when the game isn't ticking?
                // at the very least make adding and removing, force sync an update packet if it isn't ticking
                if (bin.addTicks == 0) {
                    if (!stack.isEmpty()) {
                        ItemStack remain = binSlot.insertItem(stack, Action.EXECUTE, AutomationType.MANUAL);
                        player.setItemInHand(hand, remain);
                    }
                    //Note: We set the add ticks regardless so that we can allow double right-clicking to insert items from the player's inventory
                    // without requiring them to first be holding the same item
                    bin.addTicks = 5;
                } else if (bin.addTicks > 0 && !storedStack.isEmpty()) {
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
        return ItemInteractionResult.sidedSuccess(world.isClientSide);
    }
}