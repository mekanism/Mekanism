package mekanism.common.block.basic;

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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
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
                        ItemResource binItemType = binSlot.getBinItemType();
                        //Protect against any mods that might be doing transactional logic, such as if an auto clicker validates it has enough energy before calling this method
                        try (Transaction transaction = MekanismUtils.openTransactionSafe()) {
                            int extracted = binSlot.extract(binItemType, player.isShiftKeyDown() ? binItemType.getMaxStackSize() : 1, transaction, AutomationType.MANUAL);
                            if (extracted > 0) {
                                PlayerInventoryWrapper playerInv = PlayerInventoryWrapper.of(player);
                                int inserted = playerInv.insert(binItemType, extracted, transaction);
                                transaction.commit();
                                if (inserted < extracted) {
                                    // If we couldn't insert all of it, drop the remainder.
                                    //TODO - 26.1: Would we rather use this instead of manually adding the entity?
                                    // The main different I think would be that it fires the toss event, and also that it has a pick up delay (and positioning might be different)
                                    //playerInv.drop(binItemType, extracted - inserted, false, false, transaction);
                                    Vec3 dropPos = Vec3.upFromBottomCenterOf(pos.relative(bin.getDirection()), 0.3);
                                    world.addFreshEntity(new ItemEntity(world, dropPos.x(), dropPos.y(), dropPos.z(), binItemType.toStack(extracted - inserted), 0, 0, 0));
                                } else {
                                    world.playSound(null, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                                          0.2F, ((world.getRandom().nextFloat() - world.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
                                }
                            }
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
            ItemResource binItemType = binSlot.getBinItemType();
            long binMaxSize = binSlot.capacityAsLong(binItemType);
            if (binSlot.amountAsLong() < binMaxSize) {
                //TODO - 1.21: Make add ticks and removeTicks functional somehow when the game isn't ticking?
                // at the very least make adding and removing, force sync an update packet if it isn't ticking
                if (bin.addTicks == 0) {
                    if (!stack.isEmpty()) {
                        //Protect against any mods that might be doing transactional logic, such as if an auto clicker validates it has enough energy before calling this method
                        try (Transaction transaction = MekanismUtils.openTransactionSafe()) {
                            ItemResource resource = ItemResource.of(stack);
                            int inserted = binSlot.insert(resource, stack.count(), transaction, AutomationType.MANUAL);
                            if (PlayerInventoryWrapper.of(player).getHandSlot(hand).extract(resource, inserted, transaction) == inserted) {
                                bin.addTicks = 5;
                                if (inserted > 0) {
                                    transaction.commit();
                                    return InteractionResult.SUCCESS_SERVER;
                                }
                            }
                        }
                    } else if (!binItemType.isEmpty()) {
                        //Note: We set the add ticks if the stack is empty but the bin isn't empty so that we can allow double right-clicking
                        // to insert items from the player's inventory without requiring them to first be holding the same item
                        bin.addTicks = 5;
                    }
                } else if (bin.addTicks > 0 && !binItemType.isEmpty()) {
                    //Protect against any mods that might be doing transactional logic, such as if an auto clicker validates it has enough energy before calling this method
                    try (Transaction transaction = MekanismUtils.openTransactionSafe()) {
                        boolean added = false;
                        PlayerInventoryWrapper playerInv = PlayerInventoryWrapper.of(player);
                        ResourceHandler<ItemResource> playerInvHandler = playerInv.getMainSlots();
                        for (int slot = 0, size = playerInvHandler.size(); slot < size; slot++) {
                            ItemResource itemType = playerInvHandler.getResource(slot);
                            if (!itemType.isEmpty()) {
                                int toInsert = playerInvHandler.getAmountAsInt(slot);
                                int inserted = binSlot.insert(itemType, toInsert, transaction, AutomationType.MANUAL);
                                if (inserted > 0) {
                                    //If we are able to insert the item into the bin, try extracting it from the player's handler
                                    int extracted = playerInvHandler.extract(itemType, inserted, transaction);
                                    if (inserted == extracted) {
                                        //Validate we were actually able to extract the amount we inserted
                                        added = true;
                                        if (binSlot.amountAsLong() == binMaxSize) {
                                            break;
                                        }
                                    }
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