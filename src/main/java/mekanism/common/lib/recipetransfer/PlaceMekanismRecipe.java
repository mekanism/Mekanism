package mekanism.common.lib.recipetransfer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.ItemStackChemicalToObjectRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.SingleInputRecipe;
import mekanism.api.recipes.SingleInputRecipe.ItemInputRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.resource.IResourceContainer;
import mekanism.common.Mekanism;
import mekanism.common.recipe.lookup.IRecipeLookupHandler.RecipeContainers;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookMenu.PostPlaceAction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public class PlaceMekanismRecipe {

    /// Fallback to placing the ghost recipe as we don't require clearing tanks to render, and if we are going to return the default action then we will try to clear the
    /// item slots
    private static final PostPlaceAction DEFAULT_ACTION = PostPlaceAction.PLACE_GHOST_RECIPE;

    public static <RECIPE extends MekanismRecipe<?, ?, ?>> RecipeBookMenu.PostPlaceAction placeRecipe(PlayerInventoryWrapper playerInventory, RECIPE recipe,
          boolean useMaxItems, boolean allowDroppingItemsToClear, RecipeContainers recipeContainers, TransactionContext transaction, AutomationType automationType) {
        RecipeBookPlacementHelper helper = new RecipeBookPlacementHelper(playerInventory, useMaxItems, allowDroppingItemsToClear);
        PostPlaceAction action = switch (recipe) {
            case ItemInputRecipe<?, ?, ?> itemInputRecipe -> {
                List<? extends IResourceContainer<?>> inputs = recipeContainers.inputs();
                if (inputs.size() == 1 && inputs.getFirst() instanceof IInventorySlot inputSlot) {
                    yield placeRecipe(helper, itemInputRecipe.getInput(), inputSlot, transaction, automationType);
                }
                Mekanism.logger.error("Encountered ItemInputRecipe recipe ({}) but recipe containers were not as expected did not match", recipe.getClass().getName(), new Exception());
                yield DEFAULT_ACTION;
            }
            case SingleInputRecipe.ChemicalInputRecipe<?, ?, ?> chemicalInputRecipe -> {
                List<? extends IResourceContainer<?>> inputs = recipeContainers.inputs();
                if (inputs.size() == 1 && inputs.getFirst() instanceof IChemicalTank inputTank) {
                    //TODO: Implement?
                    yield DEFAULT_ACTION;
                }
                Mekanism.logger.error("Encountered ChemicalInputRecipe recipe ({}) but recipe containers were not as expected did not match", recipe.getClass().getName(), new Exception());
                yield DEFAULT_ACTION;
            }
            case SingleInputRecipe.FluidInputRecipe<?, ?, ?> fluidInputRecipe -> {
                List<? extends IResourceContainer<?>> inputs = recipeContainers.inputs();
                if (inputs.size() == 1 && inputs.getFirst() instanceof IFluidTank inputTank) {
                    //TODO: Implement?
                    yield DEFAULT_ACTION;
                }
                Mekanism.logger.error("Encountered FluidInputRecipe recipe ({}) but recipe containers were not as expected did not match", recipe.getClass().getName(), new Exception());
                yield DEFAULT_ACTION;
            }
            case RotaryRecipe rotaryRecipe -> {
                List<? extends IResourceContainer<?>> inputs = recipeContainers.inputs();
                if (inputs.size() == 1) {
                    switch (inputs.getFirst()) {
                        case IChemicalTank inputTank when rotaryRecipe.hasChemicalToFluid() -> {
                            //TODO: Implement?
                            yield DEFAULT_ACTION;
                        }
                        case IFluidTank inputTank when rotaryRecipe.hasFluidToChemical() -> {
                            //TODO: Implement?
                            yield DEFAULT_ACTION;
                        }
                        default -> {
                        }
                    }
                }
                Mekanism.logger.error("Encountered RotaryRecipe recipe ({}) but recipe containers were not as expected did not match", recipe.getClass().getName(), new Exception());
                yield DEFAULT_ACTION;
            }
            case CombinerRecipe combinerRecipe -> {
                List<? extends IResourceContainer<?>> inputs = recipeContainers.inputs();
                if (inputs.size() == 2 && inputs.getFirst() instanceof IInventorySlot inputSlot && inputs.getLast() instanceof IInventorySlot secondaryInput) {
                    yield placeRecipe(helper, combinerRecipe.getMainInput(), inputSlot, combinerRecipe.getExtraInput(), secondaryInput, transaction, automationType);
                }
                Mekanism.logger.error("Encountered CombinerRecipe recipe ({}) but recipe containers were not as expected did not match", recipe.getClass().getName(), new Exception());
                yield DEFAULT_ACTION;
            }
            case ItemStackChemicalToObjectRecipe<?, ?, ?> icRecipe -> {
                List<? extends IResourceContainer<?>> inputs = recipeContainers.inputs();
                if (inputs.size() == 2 && inputs.getFirst() instanceof IInventorySlot inputSlot && inputs.getLast() instanceof IChemicalTank inputTank) {
                    if (inputTank.isEmpty()) {
                        //TODO: Implement?
                        yield DEFAULT_ACTION;
                    }
                    ChemicalStack chemicalStack = inputTank.resource().toStack(inputTank.amountAsInt());
                    if (!icRecipe.getChemicalInput().test(chemicalStack)) {
                        //TODO: Implement support for switching out what chemical is stored as it doesn't match for the recipe being transferred or there isn't enough
                        yield DEFAULT_ACTION;
                    }
                    yield placeRecipe(helper, icRecipe.getItemInput(), inputSlot, transaction, automationType);
                }
                Mekanism.logger.error("Encountered ItemStackChemicalToObjectRecipe recipe ({}) but recipe containers were not as expected did not match", recipe.getClass().getName(), new Exception());
                yield DEFAULT_ACTION;
            }
            case FluidChemicalToChemicalRecipe<?, ?> fc2cRecipe -> {
                List<? extends IResourceContainer<?>> inputs = recipeContainers.inputs();
                if (inputs.size() == 2 && inputs.getFirst() instanceof IFluidTank inputFluidTank && inputs.getLast() instanceof IChemicalTank inputChemicalTank) {
                    //TODO: Implement?
                    yield DEFAULT_ACTION;
                }
                Mekanism.logger.error("Encountered FluidChemicalToChemicalRecipe recipe ({}) but recipe containers were not as expected did not match", recipe.getClass().getName(), new Exception());
                yield DEFAULT_ACTION;
            }
            case ChemicalChemicalToChemicalRecipe<?, ?> cc2cRecipe -> {
                List<? extends IResourceContainer<?>> inputs = recipeContainers.inputs();
                if (inputs.size() == 2 && inputs.getFirst() instanceof IChemicalTank inputChemicalTank && inputs.getLast() instanceof IChemicalTank secondaryInput) {
                    //TODO: Implement?
                    yield DEFAULT_ACTION;
                }
                Mekanism.logger.error("Encountered ChemicalChemicalToChemicalRecipe recipe ({}) but recipe containers were not as expected did not match", recipe.getClass().getName(), new Exception());
                yield DEFAULT_ACTION;
            }
            case PressurizedReactionRecipe reactionRecipe -> {
                List<? extends IResourceContainer<?>> inputs = recipeContainers.inputs();
                if (inputs.size() == 3 && inputs.getFirst() instanceof IInventorySlot inputSlot && inputs.get(1) instanceof IFluidTank inputFluidTank
                    && inputs.getLast() instanceof IChemicalTank inputChemicalTank) {
                    if (inputFluidTank.isEmpty() || inputChemicalTank.isEmpty()) {
                        //TODO: Implement?
                        yield DEFAULT_ACTION;
                    }
                    ChemicalStack chemicalStack = inputChemicalTank.resource().toStack(inputChemicalTank.amountAsInt());
                    if (!reactionRecipe.getInputChemical().test(chemicalStack)) {
                        //TODO: Implement support for switching out what chemical is stored as it doesn't match for the recipe being transferred or there isn't enough
                        yield DEFAULT_ACTION;
                    }
                    FluidStack fluidStack = inputFluidTank.resource().toStack(inputFluidTank.amountAsInt());
                    if (!reactionRecipe.getInputFluid().test(fluidStack)) {
                        //TODO: Implement support for switching out what fluid is stored as it doesn't match for the recipe being transferred or there isn't enough
                        yield DEFAULT_ACTION;
                    }
                    yield placeRecipe(helper, reactionRecipe.getInputSolid(), inputSlot, transaction, automationType);
                }
                Mekanism.logger.error("Encountered PressurizedReactionRecipe recipe ({}) but recipe containers were not as expected did not match", recipe.getClass().getName(), new Exception());
                yield DEFAULT_ACTION;
            }
            default -> {
                Mekanism.logger.warn("Encountered recipe implementation that recipe transferring did not know how to handler: {}", recipe.getClass().getName());
                yield DEFAULT_ACTION;
            }
        };
        if (action == PostPlaceAction.PLACE_GHOST_RECIPE) {
            //Try to clear the input and output item slots so that the ghost recipe can be rendered properly
            List<IInventorySlot> slotsToClear = new ArrayList<>();
            for (IResourceContainer<?> input : recipeContainers.inputs()) {
                if (input instanceof IInventorySlot slot) {
                    slotsToClear.add(slot);
                }
            }
            for (IResourceContainer<?> output : recipeContainers.outputs()) {
                if (output instanceof IInventorySlot slot) {
                    slotsToClear.add(slot);
                }
            }
            if (!slotsToClear.isEmpty()) {
                //We have item slots present that we need to try and clear
                try (Transaction subTransaction = Transaction.open(transaction)) {
                    for (IInventorySlot slot : slotsToClear) {
                        ItemResource currentResource = slot.resource();
                        if (currentResource.isEmpty()) {
                            //Skip this slot as it is already empty
                            continue;
                        }
                        int amount = slot.amountAsInt();
                        if (slot.extract(currentResource, amount, subTransaction, automationType) != amount || !slot.isEmpty()) {
                            //If we couldn't extract it all, or if after doing so it still isn't empty, we can't handle clear it to display the ghost, return no action
                            return PostPlaceAction.NOTHING;
                        } else if (!helper.sendItemsToPlayerInventory(currentResource, amount, subTransaction)) {
                            //Abort, we can't put the items from the input slot into the player's inventory, and we are not allowed to drop them
                            return PostPlaceAction.NOTHING;
                        }
                    }
                    //We successfully moved all the items out of the crafting container
                    subTransaction.commit();
                }
            }
        }
        return action;
    }

    public static <RESULT extends @Nullable Object> RESULT placeRecipe(RecipePlacementHelper<RESULT> helper, ItemStackIngredient input, IInventorySlot inputSlot,
          TransactionContext transaction, AutomationType automationType) {
        return placeRecipe(helper, List.of(input), List.of(inputSlot), transaction, automationType);
    }

    public static <RESULT extends @Nullable Object> RESULT placeRecipe(RecipePlacementHelper<RESULT> helper, ItemStackIngredient input, IInventorySlot inputSlot,
          ItemStackIngredient extraInput, IInventorySlot extraSlot, TransactionContext transaction, AutomationType automationType) {
        return placeRecipe(helper, List.of(input, extraInput), List.of(inputSlot, extraSlot), transaction, automationType);
    }

    public static <RESULT extends @Nullable Object> RESULT placeRecipe(RecipePlacementHelper<RESULT> helper, List<ItemStackIngredient> itemIngredients,
          List<IInventorySlot> slots, TransactionContext transaction, AutomationType automationType) {
        Int2ObjectMap<ResourceStack<ItemResource>> resultState = RecipeIngredientResolver.solveFor(helper, itemIngredients, slots, transaction, automationType);
        if (resultState.isEmpty()) {
            return helper.error();
        }
        return tryTransfer(helper, slots, resultState, transaction);
    }

    public static <RESULT extends @Nullable Object> RESULT tryTransfer(RecipePlacementHelper<RESULT> helper, List<? extends IResourceContainer<?>> inputContainers,
          Int2ObjectMap<ResourceStack<ItemResource>> resultState, TransactionContext transaction) {
        boolean moveNeeded = false;
        Object2IntMap<ItemResource> resourcesToReturn = new Object2IntOpenHashMap<>();
        //Extract from the slots if necessary
        for (ObjectIterator<Entry<ResourceStack<ItemResource>>> iterator = Int2ObjectMaps.fastIterator(resultState); iterator.hasNext(); ) {
            Int2ObjectMap.Entry<ResourceStack<ItemResource>> entry = iterator.next();
            int slotUsedFor = entry.getIntKey();
            if (slotUsedFor < 0 || slotUsedFor >= inputContainers.size()) {
                //Mekanism.logger.warn("Received transfer request from: {}, with invalid slot index: {}.", player, slotUsedFor);
                return helper.error();
            }
            if (!(inputContainers.get(slotUsedFor) instanceof IInventorySlot slot)) {
                //Mekanism.logger.warn("Received transfer request from: {}, with container index: {}, but that container is not a slot.", player, slotUsedFor);
                return helper.error();
            }
            ItemResource resource = entry.getValue().resource();
            ItemResource currentType = slot.resource();
            if (!currentType.isEmpty()) {
                int stored = slot.amountAsInt();
                if (currentType.equals(resource)) {
                    int target = entry.getValue().amount();
                    if (stored == target) {
                        //Remove the iterator, no reason to try and insert into it for the second loop
                        iterator.remove();
                    } else if (stored > target) {
                        //We need to remove some from the slot and put it into limbo
                        int toExtract = target - stored;
                        if (toExtract <= 0) {
                            Mekanism.logger.warn("Tried to transfer {} of {} out of slot: {}", toExtract, resource, stored);
                            return helper.error();
                        }
                        if (slot.extract(currentType, toExtract, transaction, AutomationType.MANUAL) < toExtract) {
                            //Bail, couldn't remove input item
                            //Mekanism.logger.warn("Received transfer request from: {}, with slot index: {}, but could not extract partial contents from that slot.", player, slotUsedFor);
                            return helper.error();
                        }
                        moveNeeded = true;
                        resourcesToReturn.mergeInt(currentType, toExtract, Integer::sum);
                    }
                } else {
                    //Resource is different, we have to start by trying to extract it
                    if (slot.extract(currentType, stored, transaction, AutomationType.MANUAL) < stored || !slot.isEmpty()) {
                        //Bail, couldn't remove input item
                        //Mekanism.logger.warn("Received transfer request from: {}, with slot index: {}, but could not extract contents from that slot.", player, slotUsedFor);
                        return helper.error();
                    }
                    moveNeeded = true;
                    resourcesToReturn.mergeInt(currentType, stored, Integer::sum);
                }
            }
        }
        //Insert into the slots
        for (ObjectIterator<Int2ObjectMap.Entry<ResourceStack<ItemResource>>> iterator = Int2ObjectMaps.fastIterator(resultState); iterator.hasNext(); ) {
            moveNeeded = true;
            Int2ObjectMap.Entry<ResourceStack<ItemResource>> entry = iterator.next();
            int slotUsedFor = entry.getIntKey();
            IInventorySlot slot = (IInventorySlot) inputContainers.get(slotUsedFor);
            ItemResource resource = entry.getValue().resource();
            int amountToTransfer = entry.getValue().amount() - slot.amountAsInt();
            //We extract above if the target is smaller than the amount, or if it is equal then we remove the iterator
            assert amountToTransfer > 0;
            if (resourcesToReturn.containsKey(resource)) {
                int inLimbo = resourcesToReturn.getInt(resource);
                int available = Math.min(inLimbo, amountToTransfer);
                if (slot.insert(resource, available, transaction, AutomationType.MANUAL) != available) {
                    /*Mekanism.logger.warn("Received transfer request from: {}, with slot index: {}, but could not move the expected items ({} of {}) from another slot to that slot.",
                          player, slotUsedFor, available, resource);*/
                    return helper.error();
                } else if (amountToTransfer < inLimbo) {
                    resourcesToReturn.put(resource, inLimbo - amountToTransfer);
                    continue;
                }
                resourcesToReturn.removeInt(resource);
                if (amountToTransfer == inLimbo) {
                    continue;
                }
                amountToTransfer -= inLimbo;
            }
            if (helper.itemHandler().extract(resource, amountToTransfer, transaction) < amountToTransfer ||
                slot.insert(resource, amountToTransfer, transaction, AutomationType.MANUAL) != amountToTransfer) {
                /*Mekanism.logger.warn("Received transfer request from: {}, with slot index: {}, but could not transfer the expected items ({} of {}) from the player's inventory to that slot.",
                      player, slotUsedFor, amountToTransfer, resource);*/
                return helper.error();
            }
        }
        for (ObjectIterator<Object2IntMap.Entry<ItemResource>> iterator = Object2IntMaps.fastIterator(resourcesToReturn); iterator.hasNext(); ) {
            Object2IntMap.Entry<ItemResource> entry = iterator.next();
            int toInsert = entry.getIntValue();
            if (!helper.sendItemsToPlayerInventory(entry.getKey(), toInsert, transaction)) {
                //Mekanism.logger.warn("Received transfer request from: {}, but could not return items currently in recipe slots to the player.", player);
                return helper.notEnoughRoom();
            }
        }
        return moveNeeded ? helper.success() : helper.successNoAction();
    }

    private static class RecipeBookPlacementHelper implements RecipePlacementHelper<RecipeBookMenu.PostPlaceAction> {

        private final PlayerInventoryWrapper playerInventory;
        private final ResourceHandler<ItemResource> mainSlots;
        private final boolean allowDroppingItemsToClear;
        private final boolean useMaxItems;

        public RecipeBookPlacementHelper(PlayerInventoryWrapper playerInventory, boolean useMaxItems, boolean allowDroppingItemsToClear) {
            this.playerInventory = playerInventory;
            this.mainSlots = playerInventory.getMainSlots();
            this.allowDroppingItemsToClear = allowDroppingItemsToClear;
            this.useMaxItems = useMaxItems;
        }

        @Override
        public ResourceHandler<ItemResource> itemHandler() {
            return mainSlots;
        }

        @Override
        public boolean sendItemsToPlayerInventory(ItemResource resource, int amount, TransactionContext transaction) {
            if (allowDroppingItemsToClear) {
                playerInventory.placeItemBackInInventory(resource, amount, transaction);
                return true;
            }
            return RecipePlacementHelper.super.sendItemsToPlayerInventory(resource, amount, transaction);
        }

        @Override
        public PostPlaceAction success() {
            return PostPlaceAction.NOTHING;
        }

        @Override
        public PostPlaceAction error() {
            return DEFAULT_ACTION;
        }
        @Override
        public boolean useMaxItems() {
            return useMaxItems;
        }
    }
}