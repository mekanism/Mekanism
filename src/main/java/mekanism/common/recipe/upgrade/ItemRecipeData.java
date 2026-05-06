package mekanism.common.recipe.upgrade;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.item.block.ItemBlockPersonalStorage;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
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
    public boolean applyToStack(ItemStack stack) {
        if (slots.isEmpty()) {
            return true;
        }
        if (stack.getItem() instanceof ItemBlockPersonalStorage<?>) {
            //Add the slots in the same way we would for a PersonalStorageItemInventory and if we can transfer to the item,
            // we will copy them over directly
            List<IInventorySlot> stackSlots = new ArrayList<>();
            PersonalStorageManager.createSlots(stackSlots::add, ConstantPredicates.alwaysTrueBi(), null);
            //TODO: Improve the logic so that it maybe tries multiple different slot combinations
            IMekanismInventory outputHandler = new IMekanismInventory() {
                @Override
                public List<IInventorySlot> getContainers() {
                    return stackSlots;
                }

                @Override
                public void onContentsChanged() {
                }
            };
            if (applyToStack(outputHandler, slots)) {
                //We managed to transfer it all into valid slots, so save it as a new inventory
                return PersonalStorageManager.createInventoryFor(stack, stackSlots);
            }
            return false;
        }
        IMekanismInventory outputHandler = ContainerType.ITEM.createHandler(stack);
        //Something went wrong, fail
        return outputHandler != null && applyToStack(outputHandler, slots);
    }

    private static boolean applyToStack(IMekanismInventory outputHandler, List<IInventorySlot> dataSlots) {
        try (Transaction transaction = Transaction.openRoot()) {
            for (IInventorySlot slot : dataSlots) {
                if (!slot.isEmpty()) {
                    int amount = slot.amount();
                    int inserted = ResourceHandlerUtil.insertStacking(outputHandler, slot.getResource(), amount, transaction);
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