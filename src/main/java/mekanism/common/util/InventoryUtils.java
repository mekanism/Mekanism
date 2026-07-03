package mekanism.common.util;

import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import mekanism.api.AutomationType;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.math.MathUtils;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.component.component.UpgradeAware;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.item.interfaces.IDroppableContents;
import mekanism.common.lib.inventory.HandlerTransitRequest;
import mekanism.common.lib.transaction.TransactionHelper;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public final class InventoryUtils {

    private InventoryUtils() {
    }

    /// Helper to drop the contents of an inventory when it is destroyed if it is public or the cause of the destruction has access to the inventory.
    public static void dropItemContents(ItemEntity entity, DamageSource source) {
        ItemStack stack = entity.getItem();
        Level level = entity.level();
        if (!level.isClientSide() && !stack.isEmpty()) {
            ItemAccess itemAccess = ItemAccess.forStack(stack);
            ItemResource itemType = itemAccess.getResource();
            if (source.getEntity() instanceof Player player) {
                //If the destroyer is a player use security utils to properly check for access
                if (!IItemSecurityUtils.INSTANCE.canAccess(player, itemAccess)) {
                    return;
                }
            } else if (!IItemSecurityUtils.INSTANCE.canAccess(null, itemAccess, false)) {
                // otherwise, just check against there being no known player
                return;
            }
            int scalar = itemAccess.getAmount();
            BlockPos blockPos = entity.blockPosition();
            ItemDropper<BlockPos> dropper = (lvl, pos, _, slotStack) -> lvl.addFreshEntity(new ItemEntity(lvl, pos.getX(), pos.getY(), pos.getZ(), slotStack));
            //Note: This instanceof check must be checked before the container type to allow overriding what contents can be dropped
            if (itemType.getItem() instanceof IDroppableContents inventory) {
                if (inventory.canContentsDrop(itemType)) {
                    scalar = inventory.getScalar(itemAccess);
                    List<LargeResourceStack<ItemResource>> droppedSlots;
                    try (Transaction transaction = TransactionHelper.openTransactionSafe()) {
                        droppedSlots = inventory.getDroppedSlots(itemAccess, transaction);
                        transaction.commit();
                    }
                    dropItemContents(level, blockPos, droppedSlots, scalar, dropper);
                } else {
                    //Explicitly denying dropping items
                    return;
                }
            } else if (ContainerType.ITEM.supports(itemType)) {
                dropItemContents(level, blockPos, ContainerType.ITEM.getAttachedContents(itemType), scalar, dropper);
            }
            UpgradeAware upgradeAware = itemType.get(MekanismDataComponents.UPGRADES);
            if (upgradeAware != null) {
                dropItemContents(level, blockPos, List.of(upgradeAware.inputSlot(), upgradeAware.outputSlot()), scalar, dropper, LargeResourceStack::resource, LargeResourceStack::amount);
                dropItemContents(level, blockPos, upgradeAware.upgrades().object2IntEntrySet(), scalar, dropper, entry -> UpgradeUtils.getResource(entry.getKey()),
                      Map.Entry::getValue);
            }
            IModuleContainer moduleContainer = IModuleHelper.INSTANCE.getModuleContainer(itemType);
            if (moduleContainer != null) {
                dropItemContents(level, blockPos, moduleContainer.modules(), scalar, dropper,
                      module -> ItemResource.of(module.getUntypedData().getItemHolder()), IModule::getInstalledCount);
            }
        }
    }

    private static void dropItemContents(Level level, BlockPos pos, List<LargeResourceStack<ItemResource>> slots, int scalar, ItemDropper<BlockPos> dropper) {
        dropItemContents(level, pos, slots, scalar, dropper, LargeResourceStack::resource, LargeResourceStack::amount);
    }

    private static <T> void dropItemContents(Level level, BlockPos pos, Collection<T> toDrop, int scalar, ItemDropper<BlockPos> dropper,
          Function<T, ItemResource> itemTypeExtractor, ToLongFunction<T> sizeExtractor) {
        for (T drop : toDrop) {
            ItemResource typeToDrop = itemTypeExtractor.apply(drop);
            if (!typeToDrop.isEmpty()) {
                long amount = sizeExtractor.applyAsLong(drop);
                //Note: We increase the size of the stack we are dropping based on the size of the stack we are dropping,
                // this makes it so that if there are two items that are stacked because they have the same inventory that
                // then we actually end up dropping the stack for each of the items. dropStack handles ensuring that we don't
                // drop items past their max stack size
                if (scalar > 1) {
                    amount = MathUtils.multiplyClamped(amount, scalar);
                }
                //Copy the stack as the passed slot is likely to be the actual backing slot
                dropStack(level, pos, null, typeToDrop, amount, dropper);
            }
        }
    }

    /// Helper to drop a stack that may potentially be oversized.
    ///
    /// @param itemType Item type to drop.
    /// @param amount   Amount of the item to drop.
    /// @param dropper  Called to drop the item.
    public static <POS> void dropStack(Level level, POS pos, @Nullable Direction side, ItemResource itemType, final long amount, ItemDropper<POS> dropper) {
        if (amount > Integer.MAX_VALUE) {
            //TODO: This never *really* would happen because of how our multiblock's inventories are currently setup... but maybe we should declare more explicit behavior?
            return;
        }
        int amountAsInt = Ints.saturatedCast(amount);
        int max = itemType.getMaxStackSize();
        //If we have more than a stack of the item (such as we are a bin) or some other thing that allows for compressing
        // stack counts, drop as many stacks as we need at their max size
        while (amountAsInt > max) {
            dropper.drop(level, pos, side, itemType.toStack(max));
            amountAsInt -= max;
        }
        if (amount > 0) {
            //If we have anything left to drop afterward, do so
            dropper.drop(level, pos, side, itemType.toStack(amountAsInt));
        }
    }

    /// Like [ItemStack#isSameItemSameComponents(ItemStack, ItemStack)] but empty stacks mean equal (either param). Thiakil: not sure why.
    ///
    /// @param toInsert stack a
    /// @param inSlot   stack b
    ///
    /// @return true if they are compatible
    public static boolean areItemsStackable(ItemStack toInsert, ItemResource inSlot) {
        if (toInsert.isEmpty() || inSlot.isEmpty()) {
            return true;
        }
        return inSlot.matches(toInsert);
    }

    public static HandlerTransitRequest getEjectItemMap(ResourceHandler<ItemResource> handler, List<IInventorySlot> slots, @Nullable TransactionContext transaction) {
        return getEjectItemMap(new HandlerTransitRequest(handler), slots, transaction);
    }

    @Contract("_, _, _ -> param1")
    public static <REQUEST extends HandlerTransitRequest> REQUEST getEjectItemMap(REQUEST request, List<IInventorySlot> slots, @Nullable TransactionContext transaction) {
        // shuffle the order we look at our slots to avoid ejection patterns
        List<IInventorySlot> shuffled = new ArrayList<>(slots);
        Collections.shuffle(shuffled);
        for (IInventorySlot slot : shuffled) {
            ItemResource resource = slot.resource();
            if (!resource.isEmpty()) {
                try (Transaction simulation = Transaction.open(transaction)) {
                    //Note: We are using EXTERNAL as that is what we actually end up using when performing the extraction in the end
                    int extracted = slot.extract(resource, slot.amountAsInt(), simulation, AutomationType.EXTERNAL);
                    if (extracted > 0) {
                        request.addItem(resource, extracted, slots.indexOf(slot));
                    }
                }
            }
        }
        return request;
    }

    @FunctionalInterface
    public interface ItemDropper<POS> {

        void drop(Level level, POS pos, @Nullable Direction side, ItemStack stack);
    }
}
