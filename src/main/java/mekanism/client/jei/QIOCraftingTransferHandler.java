package mekanism.client.jei;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;

//TODO - 10.1: Min JEI version of 7.6.1, or whatever the version ends up being that has the getUniqueIdentifierForStack exposure
public class QIOCraftingTransferHandler<CONTAINER extends QIOItemViewerContainer> implements IRecipeTransferHandler<CONTAINER> {

    private final IRecipeTransferHandlerHelper handlerHelper;
    private final Class<CONTAINER> containerClass;
    private final IStackHelper stackHelper;

    public QIOCraftingTransferHandler(IRecipeTransferHandlerHelper handlerHelper, IStackHelper stackHelper, Class<CONTAINER> containerClass) {
        this.handlerHelper = handlerHelper;
        this.stackHelper = stackHelper;
        this.containerClass = containerClass;
    }

    @Override
    public Class<CONTAINER> getContainerClass() {
        return containerClass;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(CONTAINER container, Object rawRecipe, IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer,
          boolean doTransfer) {
        if (!(rawRecipe instanceof IRecipe)) {
            //Ensure that we actually have an IRecipe as if we succeed we will be using the id it provides
            // to inform the server what recipe is being auto filled transfer the data to the server
            return handlerHelper.createInternalError();
        }
        byte selectedCraftingGrid = container.getSelectedCraftingGrid();
        if (selectedCraftingGrid == -1) {
            //Note: While the java docs recommend to log a message to the console when returning an internal error,
            // this isn't actually an error state here, and is just one where we want to make sure the plus button is hidden
            // as there are no crafting grids being shown
            return handlerHelper.createInternalError();
        }
        //TODO - 10.1: Do we need to validate the user has access?? Aka if they are looking at JEI does our kick out code on security level
        // change actually work properly?? Look at this when looking at https://github.com/mekanism/Mekanism-Feature-Requests/issues/153
        // We also should check users losing access when in JEI of other things we have a security system for
        //TODO: See if we can implement some sort of caching for this, especially if it turns out JEI is calling this
        // every render tick to see if it should change the transfer button rendering. We probably could add some sort of listeners to
        // inventory, QIO, and crafting window that if one changes it invalidates the cache of what ingredients are stored, though then
        // we wouldn't be able to directly modify the map as we find inputs, and also we still would have to do a lot of this comparison
        // logic, unless we can also somehow cache the recipe layout and how it interacts with the other information
        int inputCount = 0;
        Int2ObjectMap<Set<HashedItem>> hashedIngredients = new Int2ObjectArrayMap<>();
        for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : recipeLayout.getItemStacks().getGuiIngredients().entrySet()) {
            IGuiIngredient<ItemStack> ingredient = entry.getValue();
            if (ingredient.isInput()) {
                List<ItemStack> validIngredients = ingredient.getAllIngredients();
                if (!validIngredients.isEmpty()) {
                    //If there are valid ingredients, increment the count
                    inputCount++;
                    // and convert them to HashedItems
                    // Note: We use raw hashed items as none of this stuff should or will be modified while doing these checks
                    // so we may as well remove some unneeded copies
                    //TODO: Evaluate if we need to convert the hashed item for the stack to be "reduced" in what data it has
                    // to be more in line with the data that we would have on the client from the network
                    hashedIngredients.put(entry.getKey().intValue(), validIngredients.stream().map(HashedItem::raw).collect(Collectors.toSet()));
                }
            }
        }
        ResourceLocation recipeID = ((IRecipe<?>) rawRecipe).getId();
        if (inputCount > 9) {
            //I don't believe this ever will happen with a normal crafting recipe but just in case it does, error
            // if we have more than nine inputs, we check it as an extra validation step, but we don't hold off on
            // converting input ingredients to HashedItems, until we have validated this, as there should never be
            // a case where this actually happens except potentially with some really obscure modded recipe
            Mekanism.logger.warn("Error evaluating recipe transfer handler for recipe: {}, had more than 9 inputs: {}", recipeID, inputCount);
            return handlerHelper.createInternalError();
        }
        //Get all our available items in the QIO frequency, we flatten the cache to stack together items that
        // as far as the client is concerned are the same instead of keeping them UUID separated, and add all
        // the items in the currently selected crafting window and the player's inventory to our available items
        Object2LongMap<HashedItem> availableItems = container.getFlattenedCache(selectedCraftingGrid);
        Int2ObjectMap<HashedItem> matchedItems = new Int2ObjectArrayMap<>(inputCount);
        IntSet missingSlots = new IntArraySet(inputCount);
        for (Int2ObjectMap.Entry<Set<HashedItem>> entry : hashedIngredients.int2ObjectEntrySet()) {
            //TODO: Evaluate how we want to handle if an item is valid for more than one slot and one combination has it being valid
            // and one combination it is not valid. For example if we have a single piece of stone and it is valid in either slot 1 or 2
            // but slot 2 only allows for stone, and slot 1 can accept granite instead and we have granite available. When coming up with
            // a solution to this, we also will need to handle the slower comparison method.
            boolean matchFound = false;
            for (HashedItem validInput : entry.getValue()) {
                long stored = availableItems.getOrDefault(validInput, 0);
                if (stored > 0) {
                    //We found a match for this slot, reduce how much of the item we have as an input
                    if (stored == 1) {
                        availableItems.removeLong(validInput);
                    } else {
                        availableItems.put(validInput, stored - 1);
                    }
                    // mark that we found a match
                    matchFound = true;
                    // and which HashedItem corresponds with the slot's index
                    matchedItems.put(entry.getIntKey(), validInput);
                    // and stop checking the other possible inputs
                    break;
                }
            }
            if (!matchFound) {
                //If we didn't find a match for the slot, add it as a slot we may be missing
                missingSlots.add(entry.getIntKey());
            }
        }
        if (!missingSlots.isEmpty()) {
            //After doing the quicker exact match lookup checks, go through any potentially missing slots
            // and do the slower more "accurate" check of if the stacks match. This allows us to use JEI's
            // system for letting mods declare what things match when it comes down to NBT
            Map<HashedItem, String> cachedIngredientUUIDs = new HashMap<>();
            for (Object2LongMap.Entry<HashedItem> entry : availableItems.object2LongEntrySet()) {
                HashedItem storedHashedItem = entry.getKey();
                ItemStack storedItem = storedHashedItem.getStack();
                Item storedItemType = storedItem.getItem();
                long storedCount = entry.getLongValue();
                String storedItemUUID = null;
                IntSet matchingSlots = new IntArraySet(missingSlots.size());
                for (int index : missingSlots) {
                    Set<HashedItem> validIngredients = hashedIngredients.get(index);
                    for (HashedItem validIngredient : validIngredients) {
                        //Compare the raw item types
                        if (storedItemType == validIngredient.getStack().getItem()) {
                            //If they match, compute the identifiers for both stacks as needed
                            if (storedItemUUID == null) {
                                //If we haven't retrieved a UUID for the stored stack yet because none of our previous ingredients
                                // matched the basic item type, retrieve it
                                storedItemUUID = stackHelper.getUniqueIdentifierForStack(storedItem, UidContext.Recipe);
                            }
                            //Next compute the UUID for the ingredient we are missing if we haven't already calculated it
                            // either in a previous iteration or for a different slot
                            String ingredientUUID = cachedIngredientUUIDs.computeIfAbsent(validIngredient,
                                  ingredient -> stackHelper.getUniqueIdentifierForStack(ingredient.getStack(), UidContext.Recipe));
                            if (storedItemUUID.equals(ingredientUUID)) {
                                //If the items are equivalent mark that we found a matching slot
                                matchingSlots.add(index);
                                // and mark which HashedItem corresponds with the slot's index
                                matchedItems.put(index, storedHashedItem);
                                break;
                            }
                        }
                    }
                    if (matchingSlots.size() == storedCount) {
                        //If we have "used up" all of the input we have available then continue onto the next stored stack
                        break;
                    }
                }
                //Remove all slots we found matches for
                //Note: We don't bother decrementing the items from availableItems as how we loop over things means that
                // we won't be checking this entry again and by ignoring it we don't have to worry about any concurrent
                // modification exceptions
                missingSlots.removeAll(matchingSlots);
                if (missingSlots.isEmpty()) {
                    //If we have accounted for all the slots, stop checking for matches
                    break;
                }
            }
            if (!missingSlots.isEmpty()) {
                //If we have any missing slots, report that they are missing to the user and don't allow transferring
                return handlerHelper.createUserErrorForSlots(MekanismLang.JEI_MISSING_ITEMS.translate().getString(), missingSlots);
            }
        }
        //TODO: Validate we have room to shuffle items around to their final locations, maybe make use of a similar system as we
        // use for simulating transporter stack transit in terms of the InventoryInfo for how much we are able to put in what slot
        // If we have room inside the QIO we can of course just use that instead?? Also maybe handle us putting items in the
        // crafting grid making it so we have more room in the inventory to put the other stuff
        //TODO: Implement this, will it be easier to implement after we finish implementing transferring? There is a good chance of it
        // as we probably will need the reverse lookup so we know where things actually are coming from. We might be able to do a bit
        // of a short circuit though based on if we have enough room in the QIO to store the items

        if (doTransfer) {
            //TODO: Implement me
            //TODO: Do we want to do a recipe matches here? And if it already matches, leave it be?
            // if we shift click then we want to do maxTransfer so may still need to fill in the slots
            //TODO: Send packet to server with information of what to transfer and where. This packet will consist of
            // recipe id, maxTransfer, slot indices to item for that slot.
            // We will need to do a "reverse" lookup from matchedItems in the order/taking priority of:
            // crafting window input slots, player inventory, and finally QIO frequency
            // The items from the crafting window and player inventory can use slot indices, the items from the QIO should use their UUID.
            // This means if we combined multiple "identical" stacks but with different UUIDs that the client should properly send the
            // various UUIDs for the different slots.
            // Finally on the server we will need to validate they have a container and crafting window open, as well as, if the recipe is
            // valid for the items in said slots, and if it isn't fail and print out a warning (and maybe send one to the client?)
        }
        return null;
    }
}