package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.item.block.ItemBlockPersonalStorage;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

@NothingNullByDefault
public class ItemRecipeData extends ResourceRecipeData<ItemResource> {

    ItemRecipeData(List<LargeResourceStack<ItemResource>> slots) {
        super(ContainerType.ITEM, slots);
    }

    @Override
    protected ItemRecipeData createFromMerge(List<LargeResourceStack<ItemResource>> containers) {
        return new ItemRecipeData(containers);
    }

    @Override
    public boolean applyToStack(ItemAccess itemAccess) {
        if (contents.isEmpty()) {
            return true;
        }
        if (itemAccess.getResource().getItem() instanceof ItemBlockPersonalStorage<?>) {
            //Add the slots in the same way we would for a PersonalStorageItemInventory and if we can transfer to the item,
            // we will copy them over directly
            List<IInventorySlot> stackSlots = new ArrayList<>();
            PersonalStorageManager.createSlots(stackSlots::add, ConstantPredicates.alwaysTrueBi(), null);
            try (Transaction transaction = Transaction.openRoot()) {
                for (LargeResourceStack<ItemResource> content : contents) {
                    if (!content.isEmpty() && insertInto(stackSlots, content.resource(), content.amount(), transaction) < content.amount()) {
                        //If we have a remainder something failed so bail
                        return false;
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