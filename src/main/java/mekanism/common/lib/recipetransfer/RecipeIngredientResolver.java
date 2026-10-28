package mekanism.common.lib.recipetransfer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

//TODO: Expand this to supporting fluid and chemical tanks?
public class RecipeIngredientResolver {

    private RecipeIngredientResolver() {
    }

    /// @return a map of the input slot index to what the desired item for that slot is
    public static Int2ObjectMap<ResourceStack<ItemResource>> solveFor(RecipePlacementHelper<?> helper, List<ItemStackIngredient> itemIngredients,
          List<IInventorySlot> slots, TransactionContext transaction, AutomationType automationType) {
        //TODO: Can we add tests for this method
        int ingredientCount = itemIngredients.size();
        if (ingredientCount != slots.size()) {
            throw new IllegalArgumentException("Item ingredients and number of slots must be the same size.");
        }
        ResourceHandler<ItemResource> playerInventory = helper.itemHandler();

        try (Transaction simulation = Transaction.open(transaction)) {
            Object2IntMap<ItemResource> previousItems = new Object2IntOpenHashMap<>();
            for (int i = 0; i < ingredientCount; i++) {
                IInventorySlot slot = slots.get(i);
                ItemResource current = slot.resource();
                if (!current.isEmpty()) {
                    int stored = slot.amountAsInt();
                    //TODO: Short circuit for if there is only one ingredient and it is already valid and max transfer is false?
                    try (Transaction subTransaction = Transaction.open(simulation)) {
                        int extractable = slot.extract(current, stored, subTransaction, automationType);
                        if (extractable < stored || !slot.isEmpty()) {
                            if (itemIngredients.get(i).testType(current)) {
                                //Item type is valid as is, we can't extract from the input slot so just continue and treat it as the type that will be for that slot
                                continue;
                            }
                            //Current type is invalid and can't be replaced, error
                            helper.unableToClearInput(i);
                            return Int2ObjectMaps.emptyMap();
                        }
                        //We could extract it all, add it to available items
                        previousItems.mergeInt(current, stored, Integer::sum);
                        subTransaction.commit();
                    }
                }
            }
            record ResourceInfo(int available, IntList ingredientsValidFor) {
            }
            Map<ItemResource, ResourceInfo> validIngredients = new HashMap<>();
            Set<ItemResource> seenTypes = new HashSet<>();
            for (int slot = 0, size = playerInventory.size(); slot < size; slot++) {
                ItemResource resource = playerInventory.getResource(slot);
                if (!resource.isEmpty() && seenTypes.add(resource)) {
                    int max = 0;
                    IntList validFor = IntLists.emptyList();
                    for (int i = 0; i < ingredientCount; i++) {
                        ItemStackIngredient ingredient = itemIngredients.get(i);
                        IInventorySlot inventorySlot = slots.get(i);
                        if (!inventorySlot.isEmpty()) {
                            //If the slot isn't empty check if it matches the currently stored one
                            if (inventorySlot.resource().equals(resource)) {
                                if (validFor.isEmpty()) {
                                    validFor = new IntArrayList(ingredientCount);
                                }
                                validFor.add(i);
                                max += inventorySlot.getNeededAsInt(resource);
                            }
                        } else if (ingredient.testType(resource)) {
                            if (validFor.isEmpty()) {
                                validFor = new IntArrayList(ingredientCount);
                            }
                            validFor.add(i);
                            max += inventorySlot.capacityAsInt(resource);
                        }

                    }
                    //Was valid for at least one ingredient
                    if (max > 0) {
                        assert !validFor.isEmpty();
                        int previous = previousItems.getInt(resource);
                        if (previous >= max) {
                            validIngredients.put(resource, new ResourceInfo(previous, validFor));
                        } else {
                            //previous is at least zero and is smaller than max. Reduce how much we need, and then add back how much we already had
                            int extractable = playerInventory.extract(resource, max - previous, simulation) + previous;
                            if (extractable > 0) {//Validate we were actually able to extract the resource
                                validIngredients.put(resource, new ResourceInfo(extractable, validFor));
                            }
                        }
                        //No longer need it in previous items as it is now in the valid ingredient storage
                        previousItems.removeInt(resource);
                    }
                }
            }
            IntList remainingIndices = new IntArrayList();
            for (int i = 0; i < ingredientCount; i++) {
                remainingIndices.add(i);
            }
            Int2ObjectMap<ResourceStack<ItemResource>> result = new Int2ObjectOpenHashMap<>();
            boolean removedSingle;
            do {
                removedSingle = false;
                for (Iterator<Entry<ItemResource, ResourceInfo>> iter = validIngredients.entrySet().iterator(); iter.hasNext(); ) {
                    Map.Entry<ItemResource, ResourceInfo> entry = iter.next();
                    ItemResource resource = entry.getKey();
                    ResourceInfo info = entry.getValue();
                    int elements = info.ingredientsValidFor().size();
                    for (IntIterator it = remainingIndices.intIterator(); it.hasNext(); ) {
                        int ingredientIndex = it.nextInt();
                        if (!remainingIndices.contains(ingredientIndex)) {
                            //We handled this ingredient index, we can skip checking it, reduce the list so that we can then try to handle it as a single element
                            it.remove();
                            elements--;
                        }
                    }
                    if (elements == 0) {
                        //This ingredient isn't valid for any of the other spots, no need to look at it further
                        iter.remove();
                    } else if (elements == 1) {
                        int ingredientIndex = info.ingredientsValidFor().getFirst();
                        IInventorySlot slot = slots.get(ingredientIndex);
                        ItemStackIngredient ingredient = itemIngredients.get(ingredientIndex);
                        int toTransfer;
                        if (helper.useMaxItems()) {
                            toTransfer = info.available();
                        } else {
                            int stored = slot.amountAsInt();
                            toTransfer = ingredient.ingredient().count() - stored;
                            if (toTransfer <= 0) {
                                //Leave it at what it is currently given that is valid
                                result.put(ingredientIndex, new ResourceStack<>(resource, stored));
                                helper.markInputUsed(ingredientIndex, resource, stored);
                                remainingIndices.rem(ingredientIndex);
                                //We handled this ingredient, no need to look at it further
                                iter.remove();
                                removedSingle = true;
                                continue;
                            }
                            //Only bother trying to transfer until we have the necessary amount stored
                        }
                        try (Transaction subTransaction = Transaction.open(simulation)) {
                            int inserted = slot.insert(resource, toTransfer, subTransaction, automationType);
                            int stored = slot.amountAsInt();
                            //We couldn't insert it all, check if we have enough for the ingredient
                            if (inserted < toTransfer && stored < ingredient.ingredient().count()) {
                                //If we don't have the minimum amount we need, skip to trying the next ingredient
                                continue;
                            }
                            subTransaction.commit();
                            result.put(ingredientIndex, new ResourceStack<>(resource, stored));
                            helper.markInputUsed(ingredientIndex, resource, stored);
                            remainingIndices.rem(ingredientIndex);
                            //We handled this ingredient, no need to look at it further
                            iter.remove();
                            removedSingle = true;
                        }
                    }
                    if (remainingIndices.isEmpty()) {
                        break;
                    }
                }
                //Continue as long as we have removed a matching single ingredient index, and we still have things to check against
            } while (removedSingle && !remainingIndices.isEmpty() && !validIngredients.isEmpty());
            //If there are any remaining
            if (!remainingIndices.isEmpty()) {
                //TODO: Implement handling for the case of an input being valid for multiple slots at once
                /*for (Map.Entry<ItemResource, ResourceInfo> entry : validIngredients.entrySet()) {
                    ItemResource resource = entry.getKey();
                    ResourceInfo info = entry.getValue();
                    if (helper.useMaxItems()) {
                        //TODO: Implement
                    } else {
                        //TODO: This is a naive approach as maybe we can match if we try it in a different order
                        for (int ingredientIndex : info.ingredientsValidFor()) {
                            if (!remainingIndices.contains(ingredientIndex)) {
                                break;
                            }
                            IInventorySlot slot = slots.get(ingredientIndex);
                            ItemStackIngredient ingredient = itemIngredients.get(ingredientIndex);
                            int stored = slot.amountAsInt();
                            int toTransfer = ingredient.ingredient().count() - stored;
                            if (toTransfer > 0) {
                                //Only bother trying to transfer until we have the necessary amount stored if we don't already have a valid amount
                                try (Transaction subTransaction = Transaction.open(simulation)) {
                                    int inserted = slot.insert(resource, toTransfer, subTransaction, automationType);
                                    //We couldn't insert it all, check if we have enough for the ingredient
                                    if (inserted < toTransfer && stored < ingredient.ingredient().count()) {
                                        //If we don't have the minimum amount we need, skip to trying this resource for the next ingredient
                                        continue;
                                    }
                                    subTransaction.commit();
                                }
                            }
                            result.put(ingredientIndex, new ResourceStack<>(resource, stored));
                            helper.markInputUsed(ingredientIndex, resource, stored);
                            remainingIndices.rem(ingredientIndex);
                        }
                    }
                }*/
            }
            //Verify that all slots have the bare minimum number of items for the ingredient of said slot,
            // and that we added any slots that already are at the correct value to the result
            boolean error = false;
            for (int remainingIndex : remainingIndices) {
                IInventorySlot slot = slots.get(remainingIndex);
                ItemStackIngredient ingredient = itemIngredients.get(remainingIndex);
                int stored = slot.amountAsInt();
                if (stored < ingredient.ingredient().count()) {
                    error = true;
                    helper.missingItems(remainingIndex);
                } else {
                    ItemResource resource = slot.resource();
                    result.put(remainingIndex, new ResourceStack<>(resource, stored));
                    helper.markInputUsed(remainingIndex, resource, stored);
                }
            }
            return error ? Int2ObjectMaps.emptyMap() : result;
        }
    }
}