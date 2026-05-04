package mekanism.common.util;

import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import mekanism.api.AutomationType;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.attachments.component.UpgradeAware;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.container.SelectedWindowData;
import mekanism.common.item.interfaces.IDroppableContents;
import mekanism.common.lib.inventory.HandlerTransitRequest;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;

public final class InventoryUtils {

    private InventoryUtils() {
    }

    /**
     * Helper to drop the contents of an inventory when it is destroyed if it is public or the cause of the destruction has access to the inventory.
     */
    public static void dropItemContents(ItemEntity entity, DamageSource source) {
        ItemStack stack = entity.getItem();
        Level level = entity.level();
        if (!level.isClientSide() && !stack.isEmpty()) {
            if (source.getEntity() instanceof Player player) {
                //If the destroyer is a player use security utils to properly check for access
                if (!IItemSecurityUtils.INSTANCE.canAccess(player, stack)) {
                    return;
                }
            } else if (!IItemSecurityUtils.INSTANCE.canAccess(null, stack, false)) {
                // otherwise, just check against there being no known player
                return;
            }
            int scalar = stack.count();
            BlockPos blockPos = entity.blockPosition();
            ItemDropper dropper = (lvl, pos, ignored, slotStack) -> lvl.addFreshEntity(new ItemEntity(lvl, pos.getX(), pos.getY(), pos.getZ(), slotStack));
            //Note: This instanceof check must be checked before the container type to allow overriding what contents can be dropped
            if (stack.getItem() instanceof IDroppableContents inventory) {
                if (inventory.canContentsDrop(stack)) {
                    scalar = inventory.getScalar(stack);
                    dropItemContents(level, blockPos, inventory.getDroppedSlots(stack), scalar, dropper);
                } else {
                    //Explicitly denying dropping items
                    return;
                }
            } else if (ContainerType.ITEM.supports(stack)) {
                dropItemContents(level, blockPos, ContainerType.ITEM.getAttachmentContainersIfPresent(stack), scalar, dropper);
            }
            UpgradeAware upgradeAware = stack.get(MekanismDataComponents.UPGRADES);
            if (upgradeAware != null) {
                dropItemContents(level, blockPos, List.of(upgradeAware.inputSlot(), upgradeAware.outputSlot()), scalar, dropper, template -> template == null ? ItemStack.EMPTY : template.create());
                dropItemContents(level, blockPos, upgradeAware.upgrades().entrySet(), scalar, dropper, entry -> UpgradeUtils.getStack(entry.getKey(), entry.getValue()));
            }
            IModuleContainer moduleContainer = IModuleHelper.INSTANCE.getModuleContainer(stack);
            if (moduleContainer != null) {
                dropItemContents(level, blockPos, moduleContainer.modules(), scalar, dropper, module -> new ItemStack(module.getUntypedData().getItemHolder(), module.getInstalledCount()));
            }
        }
    }

    private static void dropItemContents(Level level, BlockPos pos, List<IInventorySlot> slots, int scalar, ItemDropper dropper) {
        dropItemContents(level, pos, slots, scalar, dropper, slot -> slot.getResource().toStack(slot.getCount()));
    }

    /**
     * @param stackExtractor It is expected the stack returned by the stack extractor can be safely mutated
     */
    private static <T> void dropItemContents(Level level, BlockPos pos, Collection<T> toDrop, int scalar, ItemDropper dropper,
          Function<T, ItemStack> stackExtractor) {
        for (T drop : toDrop) {
            ItemStack stackToDrop = stackExtractor.apply(drop);
            if (!stackToDrop.isEmpty()) {
                //Note: We increase the size of the stack we are dropping based on the size of the stack we are dropping,
                // this makes it so that if there are two items that are stacked because they have the same inventory that
                // then we actually end up dropping the stack for each of the items. dropStack handles ensuring that we don't
                // drop items past their max stack size
                if (scalar > 1) {
                    if (stackToDrop.count() > stackToDrop.getMaxStackSize()) {
                        //If it is already a super sized stack (for example bins), we do a bit of extra math just to ensure the value doesn't overflow
                        // though we don't bother making sure we actually drop past MAX_INT of the item, as we really would rather not be dropping that
                        // much in the first place.
                        stackToDrop.setCount(Ints.saturatedCast((long) scalar * stackToDrop.count()));
                    } else {
                        stackToDrop.setCount(scalar * stackToDrop.count());
                    }
                }
                //Copy the stack as the passed slot is likely to be the actual backing slot
                dropStack(level, pos, null, stackToDrop, dropper);
            }
        }
    }

    /**
     * Helper to drop a stack that may potentially be oversized.
     *
     * @param stack   Item Stack to drop, may be passed directly to the dropper.
     * @param dropper Called to drop the item.
     */
    public static void dropStack(Level level, BlockPos pos, Direction side, ItemStack stack, ItemDropper dropper) {
        int count = stack.count();
        int max = stack.getMaxStackSize();
        if (count > max) {
            //If we have more than a stack of the item (such as we are a bin) or some other thing that allows for compressing
            // stack counts, drop as many stacks as we need at their max size
            while (count > max) {
                dropper.drop(level, pos, side, stack.copyWithCount(max));
                count -= max;
            }
            if (count > 0) {
                //If we have anything left to drop afterward, do so
                dropper.drop(level, pos, side, stack.copyWithCount(count));
            }
        } else {
            //If we have a valid stack, we can just directly drop that instead without requiring any copies
            dropper.drop(level, pos, side, stack);
        }
    }

    /**
     * Like {@link ItemStack#isSameItemSameComponents(ItemStack, ItemStack)} but empty stacks mean equal (either param). Thiakil: not sure why.
     *
     * @param toInsert stack a
     * @param inSlot   stack b
     *
     * @return true if they are compatible
     */
    public static boolean areItemsStackable(ItemStack toInsert, ItemStack inSlot) {
        if (toInsert.isEmpty() || inSlot.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(inSlot, toInsert);
    }

    public static boolean areItemsStackable(ItemStackTemplate toInsert, ItemStack inSlot) {
        if (toInsert == null || inSlot.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(inSlot, toInsert);
    }

    //TODO - 26.1: Re-evaluate this
    public static boolean isItemHandler(Level level, BlockPos pos, Direction side) {
        return Capabilities.ITEM.getCapabilityIfLoaded(level, pos, side) != null;
    }

    public static HandlerTransitRequest getEjectItemMap(ResourceHandler<ItemResource> handler, List<IInventorySlot> slots) {
        return getEjectItemMap(new HandlerTransitRequest(handler), slots);
    }

    @Contract("_, _ -> param1")
    public static <REQUEST extends HandlerTransitRequest> REQUEST getEjectItemMap(REQUEST request, List<IInventorySlot> slots) {
        // shuffle the order we look at our slots to avoid ejection patterns
        List<IInventorySlot> shuffled = new ArrayList<>(slots);
        Collections.shuffle(shuffled);
        //TODO - 26.1: Validate if any callers are in a transactional context, also should we be doing this here or around each extract call?
        // I believe it is fine to just do this once, but maybe some slot extractions depend on what is already extracted
        try (Transaction simulation = Transaction.openRoot()) {
            for (IInventorySlot slot : shuffled) {
                ItemResource resource = slot.getResource();
                if (!resource.isEmpty()) {
                    //Note: We are using EXTERNAL as that is what we actually end up using when performing the extraction in the end
                    int extracted = slot.extract(resource, slot.getCount(), simulation, AutomationType.EXTERNAL);
                    if (extracted > 0) {
                        request.addItem(resource, extracted, slots.indexOf(slot));
                    }
                }
            }
        }
        return request;
    }

    /**
     * Helper to first try inserting ignoring empty slots, and then insert not ignoring empty slots
     *
     * @param slots          Slots to insert into
     * @param itemType       Type of item to insert.
     * @param amount         Amount of the item to insert.
     * @param transaction    The transaction that this operation is part of.
     * @param automationType The method that this slot is being interacted from.
     *
     * @return Remainder
     *
     * @see net.neoforged.neoforge.transfer.ResourceHandlerUtil#insertStacking(ResourceHandler, Resource, int, TransactionContext)
     */
    public static int insertItem(List<? extends IInventorySlot> slots, ItemResource itemType, final int amount, TransactionContext transaction, AutomationType automationType) {
        int amountToInsert = amount;
        amountToInsert -= insertItem(slots, itemType, amountToInsert, transaction, true, false, automationType);
        amountToInsert -= insertItem(slots, itemType, amountToInsert, transaction, false, false, automationType);
        //Return how much was actually inserted
        return amount - amountToInsert;
    }

    /**
     * Helper to try inserting a given amount of a resource into a list of inventory slots only inserting into either empty slots or inserting into non-empty slots.
     *
     * @param slots          Slots to insert into
     * @param itemType       Type of item to insert.
     * @param amount         Amount of the item to insert.
     * @param transaction    The transaction that this operation is part of.
     * @param ignoreEmpty    {@code true} to ignore/skip empty slots, {@code false} to ignore/skip non-empty slots.
     * @param checkAll       {@code true} to check all slots regardless of empty state. When this is {@code true}, {@code ignoreEmpty} is ignored.
     * @param automationType The method that this slot is being interacted from.
     *
     * @return Remainder
     *
     * @see mekanism.common.inventory.container.MekanismContainer#insertItem(List, ItemResource, int, TransactionContext, boolean, boolean, SelectedWindowData)
     */
    public static int insertItem(List<? extends IInventorySlot> slots, ItemResource itemType, final int amount, TransactionContext transaction, boolean ignoreEmpty,
          boolean checkAll, AutomationType automationType) {
        if (itemType.isEmpty() || amount == 0) {
            //Skip doing anything if the stack is already empty.
            // Makes it easier to chain calls, rather than having to check if the stack is empty after our previous call
            return 0;
        }
        int toInsert = amount;
        for (IInventorySlot slot : slots) {
            if (!checkAll && ignoreEmpty == slot.isEmpty()) {
                //Skip checking empty stacks if we want to ignore them, and skip non-empty stacks if we don't want ot ignore them
                continue;
            }
            toInsert -= slot.insert(itemType, toInsert, transaction, automationType);
            if (toInsert == 0) {
                break;
            }
        }
        return amount - toInsert;
    }

    @FunctionalInterface
    public interface ItemDropper {

        void drop(Level level, BlockPos pos, Direction side, ItemStack stack);
    }
}
