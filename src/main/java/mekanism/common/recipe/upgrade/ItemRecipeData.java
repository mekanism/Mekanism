package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.item.block.ItemBlockPersonalStorage;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

@NothingNullByDefault
public class ItemRecipeData extends ResourceRecipeData<ItemResource, IInventorySlot> {

    ItemRecipeData(List<IInventorySlot> slots) {
        super(ContainerType.ITEM, slots);
    }

    @Override
    protected ItemRecipeData createFromMerge(List<IInventorySlot> containers) {
        return new ItemRecipeData(containers);
    }

    @Override
    public boolean applyToStack(ItemAccess itemAccess) {
        if (containers.isEmpty()) {
            return true;
        }
        if (itemAccess.getResource().getItem() instanceof ItemBlockPersonalStorage<?>) {
            //Add the slots in the same way we would for a PersonalStorageItemInventory and if we can transfer to the item,
            // we will copy them over directly
            List<IInventorySlot> stackSlots = new ArrayList<>();
            PersonalStorageManager.createSlots(stackSlots::add, ConstantPredicates.alwaysTrueBi(), null);
            try (Transaction transaction = Transaction.openRoot()) {
                for (IInventorySlot slot : containers) {
                    if (!slot.isEmpty()) {
                        long amount = slot.amountAsLong();
                        if (insertInto(stackSlots, slot.resource(), amount, transaction) < amount) {
                            //If we have a remainder something failed so bail
                            return false;
                        }
                    }
                }
                transaction.commit();
                //We managed to transfer it all into valid slots, so save it as a new inventory
                return PersonalStorageManager.createInventoryFor(itemAccess, stackSlots);
            }
        }
        return super.applyToStack(itemAccess);
    }
}