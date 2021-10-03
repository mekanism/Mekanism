package mekanism.client.jei;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.math.MathUtils;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.content.qio.QIOCraftingTransferHelper;
import mekanism.common.content.qio.QIOCraftingTransferHelper.HashedItemSource;
import mekanism.common.content.qio.QIOCraftingTransferHelper.SingularHashedItemSource;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mekanism.common.lib.inventory.HashedItem;
import mekanism.common.network.to_server.PacketQIOFillCraftingWindow;
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
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.util.ResourceLocation;

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
        if (!(rawRecipe instanceof ICraftingRecipe)) {
            //Ensure that we actually have an IRecipe as if we succeed we will be using the id it provides
            // to inform the server what recipe is being auto-filled transfer the data to the server
            //Note: Technically we could check this as IRecipe, but we do ICraftingRecipe as it really should be
            // a crafting recipe, and if it isn't the server won't know how to transfer it anyways
            return handlerHelper.createInternalError();
        }
        byte selectedCraftingGrid = container.getSelectedCraftingGrid();
        if (selectedCraftingGrid == -1) {
            //Note: While the java docs recommend logging a message to the console when returning an internal error,
            // this isn't actually an error state here, and is just one where we want to make sure the plus button is hidden
            // as there are no crafting grids being shown
            return handlerHelper.createInternalError();
        }
        //TODO - 10.1: Do we need to validate the user has access?? Aka if they are looking at JEI does our kick out code on security level
        // change actually work properly?? Look at this when looking at https://github.com/mekanism/Mekanism-Feature-Requests/issues/153
        // We also should check users losing access when in JEI of other things we have a security system for
        //TODO: It may be nice to eventually implement some sort of caching for this, it isn't drastically needed because JEI is smart
        // and only calls it once per recipe to decide if it should display the button rather than say calling it every render tick in
        // case something changed and the render state should be different. We probably could add some sort of listeners to
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
                    // Note: we use a linked hash set to preserve the order of the ingredients as done in JEI
                    LinkedHashSet<HashedItem> representations = new LinkedHashSet<>();
                    ItemStack displayed = ingredient.getDisplayedIngredient();
                    //Note: We use raw hashed items as none of this stuff should or will be modified while doing these checks
                    // so we may as well remove some unneeded copies
                    //TODO: If there are any issues that eventually come up due to things not matching, we should evaluate
                    // converting the hashed item for the stack into the "reduced" form that the server would normally send
                    // to the client, as the extended data for what is in the QIO and the player's inventory uses the normal
                    // item stack networking method that allows less NBT to be transferred. This means that if the client's
                    // stack gets extra data somehow it may not line up, though most likely it will
                    if (displayed != null) {
                        //Start by adding the displayed ingredient if there is one to prioritize it
                        representations.add(HashedItem.raw(displayed));
                    }
                    //Then add all valid ingredients in the order they appear in JEI. Because we are using a set
                    // we will just end up merging with the displayed ingredient when we get to it as a valid ingredient
                    for (ItemStack validIngredient : validIngredients) {
                        representations.add(HashedItem.raw(validIngredient));
                    }
                    //Note: We decrement the index by one because JEI uses the first index for the output
                    hashedIngredients.put(entry.getKey() - 1, representations);
                }
            }
        }
        ResourceLocation recipeID = ((ICraftingRecipe) rawRecipe).getId();
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
        QIOCraftingTransferHelper qioTransferHelper = container.getTransferHelper(player, selectedCraftingGrid);
        //Note: We do this in a reversed manner (HashedItem -> slots, vs slot -> HashedItem) so that we can more easily
        // calculate the split for how we handle maxTransfer by quickly being able to see how many of each type we have
        Map<HashedItem, IntList> matchedItems = new HashMap<>(inputCount);
        IntSet missingSlots = new IntArraySet(inputCount);
        for (Int2ObjectMap.Entry<Set<HashedItem>> entry : hashedIngredients.int2ObjectEntrySet()) {
            //TODO: Eventually we probably will want to add in some handling for if an item is valid for more than one slot and one combination
            // has it being valid and one combination it is not valid. For example if we have a single piece of stone and it is valid in either
            // slot 1 or 2 but slot 2 only allows for stone, and slot 1 can accept granite instead and we have granite available. When coming
            // up with a solution to this, we also will need to handle the slower comparison method, and make sure that if maxTransfer is true
            // then we pick the one that has the most elements we can assign to all slots evenly so that we can craft as many things as possible.
            // We currently don't bother with any handling related to this as JEI's own transfer handler it registers for things like the crafting
            // table don't currently handle this, though it is something that would be nice to handle and is something I believe vanilla's recipe
            // book transfer handler is able to do (RecipeItemHelper/ServerRecipePlayer)
            boolean matchFound = false;
            for (HashedItem validInput : entry.getValue()) {
                HashedItemSource source = qioTransferHelper.getSource(validInput);
                if (source != null && source.hasMoreRemaining()) {
                    //We found a match for this slot, reduce how much of the item we have as an input
                    source.matchFound();
                    // mark that we found a match
                    matchFound = true;
                    // and which HashedItem the slot's index corresponds to
                    matchedItems.computeIfAbsent(validInput, item -> new IntArrayList()).add(entry.getIntKey());
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
            for (Map.Entry<HashedItem, HashedItemSource> entry : qioTransferHelper.reverseLookup.entrySet()) {
                HashedItemSource source = entry.getValue();
                if (source.hasMoreRemaining()) {
                    //Only look at the source if we still have more items available in it
                    HashedItem storedHashedItem = entry.getKey();
                    ItemStack storedItem = storedHashedItem.getStack();
                    Item storedItemType = storedItem.getItem();
                    String storedItemUUID = null;
                    for (IntIterator missingIterator = missingSlots.iterator(); missingIterator.hasNext(); ) {
                        int index = missingIterator.nextInt();
                        for (HashedItem validIngredient : hashedIngredients.get(index)) {
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
                                    //If the items are equivalent, reduce how much of the item we have as an input
                                    source.matchFound();
                                    // unmark that the slot is missing a match
                                    missingIterator.remove();
                                    // and mark which HashedItem the slot's index corresponds to
                                    matchedItems.computeIfAbsent(storedHashedItem, item -> new IntArrayList()).add(index);
                                    // and stop checking the other possible inputs
                                    break;
                                }
                            }
                        }
                        if (!source.hasMoreRemaining()) {
                            //If we have "used up" all the input we have available then continue onto the next stored stack
                            break;
                        }
                    }
                    if (missingSlots.isEmpty()) {
                        //If we have accounted for all the slots, stop checking for matches
                        break;
                    }
                }
            }
            if (!missingSlots.isEmpty()) {
                //If we have any missing slots, report that they are missing to the user and don't allow transferring
                // Note: We have to shift this back up by one as we shifted the indices earlier to make them easier to work with
                return handlerHelper.createUserErrorForSlots(MekanismLang.JEI_MISSING_ITEMS.translate(), missingSlots.stream().map(i -> i + 1).collect(Collectors.toList()));
            }
        }
        //TODO - 10.1: Validate we have room to shuffle items around to their final locations. We can use the first part of the server shuffle check
        if (doTransfer) {
            int toTransfer;
            if (maxTransfer) {
                //Calculate how much we can actually transfer if we want to transfer as many full sets as possible
                long maxToTransfer = Long.MAX_VALUE;
                for (Map.Entry<HashedItem, IntList> entry : matchedItems.entrySet()) {
                    HashedItem hashedItem = entry.getKey();
                    HashedItemSource source = qioTransferHelper.getSource(hashedItem);
                    if (source == null) {
                        //If something went wrong, and we don't actually have the item we think we do, error
                        return invalidSource(hashedItem.getStack());
                    }
                    int maxStack = hashedItem.getStack().getMaxStackSize();
                    //If we have something that only stacks to one, such as a bucket. Don't limit the max stack size
                    // of other items to one
                    long max = maxStack == 1 ? maxToTransfer : Math.min(maxToTransfer, maxStack);
                    //Note: This will always be at least one as the int list should not be able to become
                    // larger than the number of items we have available
                    maxToTransfer = Math.min(max, source.getAvailable() / entry.getValue().size());
                }
                toTransfer = MathUtils.clampToInt(maxToTransfer);
            } else {
                toTransfer = 1;
            }
            //TODO - 10.1: Do we want to do a recipe "matches" here? And if it already matches, leave it be?
            // if we shift click then we want to do maxTransfer so may still need to fill in the slots
            // If we are doing so we should probably either do so higher up (before trying to match the various stacks)
            // the one potential "issue" with that is if we want to allow changing the exact match of ingredients to a newly shown one
            // then this would not work like that
            Byte2ObjectMap<List<SingularHashedItemSource>> sources = new Byte2ObjectArrayMap<>(inputCount);
            for (Map.Entry<HashedItem, IntList> entry : matchedItems.entrySet()) {
                HashedItem hashedItem = entry.getKey();
                HashedItemSource source = qioTransferHelper.getSource(hashedItem);
                if (source == null) {
                    //If something went wrong, and we don't actually have the item we think we do, error
                    return invalidSource(hashedItem.getStack());
                }
                //Cap the amount to transfer at the max tack size. This way we allow for transferring buckets
                // and other stuff with it. This only actually matters if the max stack size is one, due to
                // the logic done above when calculating how much to transfer, but we do this regardless here
                // as there is no reason not to and then if we decide to widen it up we only have to change one spot
                int transferAmount = Math.min(toTransfer, hashedItem.getStack().getMaxStackSize());
                for (int slot : entry.getValue()) {
                    //Try to use the item and figure out where it is coming from
                    List<SingularHashedItemSource> actualSources = source.use(transferAmount);
                    if (actualSources.isEmpty()) {
                        //If something went wrong, and we don't actually have enough of the item for some reason, error
                        return invalidSource(hashedItem.getStack());
                    }
                    //Note: We cast directly to a byte as it should always fit within one
                    sources.put((byte) slot, actualSources);
                }
            }
            Mekanism.packetHandler.sendToServer(new PacketQIOFillCraftingWindow(recipeID, maxTransfer, sources));
        }
        return null;
    }

    private IRecipeTransferError invalidSource(@Nonnull ItemStack stack) {
        Mekanism.logger.warn("Error finding source for: {} with nbt: {}. This should not be possible happen.", stack.getItem(), stack.getTag());
        return handlerHelper.createInternalError();
    }
}