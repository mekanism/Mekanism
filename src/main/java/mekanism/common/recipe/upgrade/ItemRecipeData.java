package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.ComponentBackedResourceHandler;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.item.block.ItemBlockPersonalStorage;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.util.InventoryUtils;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ItemRecipeData implements RecipeUpgradeData<ItemRecipeData> {

    private final List<IInventorySlot> slots;

    ItemRecipeData(List<IInventorySlot> slots) {
        this.slots = slots;
    }

    @Nullable
    @Override
    public ItemRecipeData merge(ItemRecipeData other) {
        List<IInventorySlot> allSlots = new ArrayList<>(slots);
        allSlots.addAll(other.slots);
        return new ItemRecipeData(allSlots);
    }

    @Override
    public boolean applyToStack(ItemAccess itemAccess) {
        if (slots.isEmpty()) {
            return true;
        }
        if (itemAccess.getResource().getItem() instanceof ItemBlockPersonalStorage<?>) {
            //Add the slots in the same way we would for a PersonalStorageItemInventory and if we can transfer to the item,
            // we will copy them over directly
            List<IInventorySlot> stackSlots = new ArrayList<>();
            PersonalStorageManager.createSlots(stackSlots::add, ConstantPredicates.alwaysTrueBi(), null);
            //TODO: Improve the logic so that it maybe tries multiple different slot combinations
            if (applyToStack(stackSlots, slots)) {
                //We managed to transfer it all into valid slots, so save it as a new inventory
                return PersonalStorageManager.createInventoryFor(itemAccess, stackSlots);
            }
            return false;
        }
        ComponentBackedResourceHandler<ItemResource, IInventorySlot> outputHandler = ContainerType.ITEM.createHandler(itemAccess);
        //Something went wrong, fail
        return outputHandler != null && applyToStack(outputHandler.getContainers(), slots);
    }

    private static boolean applyToStack(List<IInventorySlot> outputSlots, List<IInventorySlot> dataSlots) {
        try (Transaction transaction = Transaction.openRoot()) {
            for (IInventorySlot slot : dataSlots) {
                if (!slot.isEmpty()) {
                    int amount = slot.amountAsInt();
                    //TODO - 26.1: The automation type here doesn't matter because we create slots that are always allowed to be interacted with
                    // but we should decide what one makes the most sense (probably whatever we decide to use for IMekanismResourceHandler#insert's default automation type
                    //TODO - 26.1: How does this work for bins if they are configured to more than max int? Do we need to special case them?
                    int inserted = InventoryUtils.insertItem(outputSlots, slot.resource(), amount, transaction, AutomationType.MANUAL);
                    if (inserted < amount) {
                        //If we have a remainder something failed so bail
                        return false;
                    }
                }
            }
            transaction.commit();
            return true;
        }
    }
}