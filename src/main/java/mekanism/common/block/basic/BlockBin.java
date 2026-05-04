package mekanism.common.block.basic;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.tile.TileEntityBin;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

public class BlockBin extends BlockTile<TileEntityBin, BlockTypeTile<TileEntityBin>> {

    public BlockBin(BlockTypeTile<TileEntityBin> type, BlockBehaviour.Properties properties) {
        super(type, properties);
    }

    @Override
    protected void attack(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player) {
        if (!world.isClientSide()) {
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
                                MekanismUtils.logMismatchedStackSize(binSlot.shrinkStack(stack.count(), Action.EXECUTE), stack.count());
                            }
                        } else {
                            stack = binSlot.getResource().toStack();
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
                                  0.2F, ((world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                        }
                    }
                }
            }
        }
    }

    @NotNull
    @Override
    protected InteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player,
          @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        TileEntityBin bin = WorldUtils.getTileEntity(TileEntityBin.class, world, pos);
        if (bin == null) {
            //No tile, we can just skip trying to use without an item
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        InteractionResult wrenchResult = bin.tryWrench(state, player, stack).getInteractionResult();
        if (wrenchResult != InteractionResult.PASS) {
            return wrenchResult;
        } else if (hit.getDirection() != bin.getDirection()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (stack.isEmpty() && player.isShiftKeyDown()) {
            return bin.toggleLock() ? InteractionResult.SUCCESS_SERVER : InteractionResult.FAIL;
        } else if (!world.isClientSide()) {
            BinInventorySlot binSlot = bin.getBinSlot();
            int binMaxSize = binSlot.getCurrentLimit();
            if (binSlot.getCount() < binMaxSize) {
                ItemResource binItemType = binSlot.getBinItemType();
                //TODO - 1.21: Make add ticks and removeTicks functional somehow when the game isn't ticking?
                // at the very least make adding and removing, force sync an update packet if it isn't ticking
                if (bin.addTicks == 0) {
                    if (!stack.isEmpty()) {
                        try (Transaction transaction = Transaction.openRoot()) {
                            int toInsert = stack.count();
                            int inserted = binSlot.insert(ItemResource.of(stack), toInsert, transaction, AutomationType.MANUAL);
                            bin.addTicks = 5;
                            if (inserted > 0) {
                                transaction.commit();
                                return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(stack.copyWithCount(toInsert - inserted));
                            }
                        }
                    } else if (!binItemType.isEmpty()) {
                        //Note: We set the add ticks if the stack is empty but the bin isn't empty so that we can allow double right-clicking
                        // to insert items from the player's inventory without requiring them to first be holding the same item
                        bin.addTicks = 5;
                    }
                } else if (bin.addTicks > 0 && !binItemType.isEmpty()) {
                    try (Transaction transaction = Transaction.openRoot()) {
                        boolean added = false;
                        for (ItemStack stackToAdd : player.getInventory().getNonEquipmentItems()) {
                            if (!stackToAdd.isEmpty() && binItemType.matches(stackToAdd)) {
                                stackToAdd.shrink(binSlot.insert(binItemType, stackToAdd.count(), transaction, AutomationType.MANUAL));
                                added = true;
                                if (binSlot.getCount() == binMaxSize) {
                                    break;
                                }
                            }
                        }
                        if (added) {
                            transaction.commit();
                            bin.addTicks = 5;
                            player.containerMenu.sendAllDataToRemote();
                        }
                    }
                }
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}