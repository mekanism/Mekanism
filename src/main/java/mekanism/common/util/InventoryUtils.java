package mekanism.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
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
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class InventoryUtils {

    private InventoryUtils() {
    }

    /**
     * Helper to drop the contents of an inventory when it is destroyed if it is public or the cause of the destruction has access to the inventory.
     */
    public static void dropItemContents(ItemEntity entity, DamageSource source) {
        ItemStack stack = entity.getItem();
        Level level = entity.level();
        if (!level.isClientSide && !stack.isEmpty()) {
            if (source.getEntity() instanceof Player player) {
                //If the destroyer is a player use security utils to properly check for access
                if (!IItemSecurityUtils.INSTANCE.canAccess(player, stack)) {
                    return;
                }
            } else if (!IItemSecurityUtils.INSTANCE.canAccess(null, stack, false)) {
                // otherwise, just check against there being no known player
                return;
            }
            int scalar = stack.getCount();
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
                dropItemContents(level, blockPos, List.of(upgradeAware.inputSlot(), upgradeAware.outputSlot()), scalar, dropper, ItemStack::copy);
                dropItemContents(level, blockPos, upgradeAware.upgrades().entrySet(), scalar, dropper, entry -> UpgradeUtils.getStack(entry.getKey(), entry.getValue()));
            }
            IModuleContainer moduleContainer = IModuleHelper.INSTANCE.getModuleContainer(stack);
            if (moduleContainer != null) {
                dropItemContents(level, blockPos, moduleContainer.modules(), scalar, dropper, module -> new ItemStack(module.getUntypedData().getItemHolder(), module.getInstalledCount()));
            }
        }
    }

    private static void dropItemContents(Level level, BlockPos pos, List<IInventorySlot> slots, int scalar, ItemDropper dropper) {
        dropItemContents(level, pos, slots, scalar, dropper, slot -> slot.getStack().copy());
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
                    if (stackToDrop.getCount() > stackToDrop.getMaxStackSize()) {
                        //If it is already a super sized stack (for example bins), we do a bit of extra math just to ensure the value doesn't overflow
                        // though we don't bother making sure we actually drop past MAX_INT of the item, as we really would rather not be dropping that
                        // much in the first place.
                        stackToDrop.setCount(MathUtils.clampToInt((long) scalar * stackToDrop.getCount()));
                    } else {
                        stackToDrop.setCount(scalar * stackToDrop.getCount());
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
        int count = stack.getCount();
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

    public static boolean isItemHandler(Level level, BlockPos pos, Direction side) {
        return Capabilities.ITEM.getCapabilityIfLoaded(level, pos, side) != null;
    }

    public static HandlerTransitRequest getEjectItemMap(IItemHandler handler, List<IInventorySlot> slots) {
        return getEjectItemMap(new HandlerTransitRequest(handler), slots);
    }

    @Contract("_, _ -> param1")
    public static <REQUEST extends HandlerTransitRequest> REQUEST getEjectItemMap(REQUEST request, List<IInventorySlot> slots) {
        // shuffle the order we look at our slots to avoid ejection patterns
        List<IInventorySlot> shuffled = new ArrayList<>(slots);
        Collections.shuffle(shuffled);
        for (IInventorySlot slot : shuffled) {
            //Note: We are using EXTERNAL as that is what we actually end up using when performing the extraction in the end
            ItemStack simulatedExtraction = slot.extractItem(slot.getCount(), Action.SIMULATE, AutomationType.EXTERNAL);
            if (!simulatedExtraction.isEmpty()) {
                request.addItem(simulatedExtraction, slots.indexOf(slot));
            }
        }
        return request;
    }

    /**
     * Helper to first try inserting ignoring empty slots, and then insert not ignoring empty slots
     *
     * @param slots          Slots to insert into
     * @param stack          Stack to insert (do not modify).
     * @param action         The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param automationType The method that this slot is being interacted from.
     *
     * @return Remainder
     *
     * @see ItemHandlerHelper#insertItemStacked(IItemHandler, ItemStack, boolean)
     */
    public static ItemStack insertItem(List<? extends IInventorySlot> slots, @NotNull ItemStack stack, Action action, AutomationType automationType) {
        stack = insertItem(slots, stack, true, false, action, automationType);
        return insertItem(slots, stack, false, false, action, automationType);
    }

    /**
     * Helper to try inserting a stack into a list of inventory slots only inserting into either empty slots or inserting into non-empty slots.
     *
     * @param slots          Slots to insert into
     * @param stack          Stack to insert (do not modify).
     * @param ignoreEmpty    {@code true} to ignore/skip empty slots, {@code false} to ignore/skip non-empty slots.
     * @param checkAll       {@code true} to check all slots regardless of empty state. When this is {@code true}, {@code ignoreEmpty} is ignored.
     * @param action         The action to perform, either {@link Action#EXECUTE} or {@link Action#SIMULATE}
     * @param automationType The method that this slot is being interacted from.
     *
     * @return Remainder
     *
     * @see mekanism.common.inventory.container.MekanismContainer#insertItem(List, ItemStack, boolean, boolean, SelectedWindowData, Action)
     */
    @NotNull
    public static ItemStack insertItem(List<? extends IInventorySlot> slots, @NotNull ItemStack stack, boolean ignoreEmpty, boolean checkAll, Action action,
          AutomationType automationType) {
        if (stack.isEmpty()) {
            //Skip doing anything if the stack is already empty.
            // Makes it easier to chain calls, rather than having to check if the stack is empty after our previous call
            return stack;
        }
        for (IInventorySlot slot : slots) {
            if (!checkAll && ignoreEmpty == slot.isEmpty()) {
                //Skip checking empty stacks if we want to ignore them, and skip non-empty stacks if we don't want ot ignore them
                continue;
            }
            stack = slot.insertItem(stack, action, automationType);
            if (stack.isEmpty()) {
                break;
            }
        }
        return stack;
    }

    public interface ItemDropper {

        void drop(Level level, BlockPos pos, Direction side, ItemStack stack);
    }
}
