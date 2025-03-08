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
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
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
    public boolean applyToStack(HolderLookup.Provider provider, ItemStack stack) {
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
                @NotNull
                @Override
                public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
                    return stackSlots;
                }

                @Override
                public void onContentsChanged() {
                }
            };
            if (applyToStack(outputHandler, slots)) {
                //We managed to transfer it all into valid slots, so save it as a new inventory
                return PersonalStorageManager.createInventoryFor(provider, stack, stackSlots);
            }
            return false;
        }
        IMekanismInventory outputHandler = ContainerType.ITEM.createHandler(stack);
        //Something went wrong, fail
        return outputHandler != null && applyToStack(outputHandler, slots);
    }

    static boolean applyToStack(IMekanismInventory outputHandler, List<IInventorySlot> dataSlots) {
        for (IInventorySlot slot : dataSlots) {
            if (!slot.isEmpty() && !ItemHandlerHelper.insertItemStacked(outputHandler, slot.getStack(), false).isEmpty()) {
                //If we have a remainder something failed so bail
                return false;
            }
        }
        return true;
    }
}