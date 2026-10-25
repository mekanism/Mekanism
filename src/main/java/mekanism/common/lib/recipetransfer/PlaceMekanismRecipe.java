package mekanism.common.lib.recipetransfer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class PlaceMekanismRecipe {

    /// Fallback to placing the ghost recipe as we don't require clearing tanks to render, and if we are going to return the default action then we will try to clear the
    /// item slots
    private static final PostPlaceAction DEFAULT_ACTION = PostPlaceAction.PLACE_GHOST_RECIPE;

    public static <RECIPE extends MekanismRecipe<?, ?, ?>> RecipeBookMenu.PostPlaceAction placeRecipe(PlayerInventoryWrapper playerInventory, RECIPE recipe,
          boolean useMaxItems, boolean allowDroppingItemsToClear, RecipeContainers recipeContainers, TransactionContext transaction, AutomationType automationType) {
        PostPlaceAction action = switch (recipe) {
            case ItemInputRecipe<?, ?, ?> itemInputRecipe -> {
                List<? extends IResourceContainer<?>> inputs = recipeContainers.inputs();
                if (inputs.size() == 1 && inputs.getFirst() instanceof IInventorySlot inputSlot) {
                    yield placeRecipe(playerInventory, itemInputRecipe.getInput(), useMaxItems, allowDroppingItemsToClear, inputSlot, transaction, automationType);
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
                    //TODO - 26.3: Figure out how to implement this
                    yield DEFAULT_ACTION;
                    //yield placeRecipe(playerInventory, combinerRecipe.getMainInput(), useMaxItems, allowDroppingItemsToClear, inputSlot, transaction, automationType);
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
                    yield placeRecipe(playerInventory, icRecipe.getItemInput(), useMaxItems, allowDroppingItemsToClear, inputSlot, transaction, automationType);
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
                    yield placeRecipe(playerInventory, reactionRecipe.getInputSolid(), useMaxItems, allowDroppingItemsToClear, inputSlot, transaction, automationType);
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
                    ResourceHandler<ItemResource> mainSlots = playerInventory.getMainSlots();
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
                        } else if (allowDroppingItemsToClear) {
                            //Place it back, this will drop if necessary so we don't have to worry about amounts
                            playerInventory.placeItemBackInInventory(currentResource, amount, subTransaction);
                        } else if (mainSlots.insert(currentResource, amount, subTransaction) < amount) {
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

    private static RecipeBookMenu.PostPlaceAction placeRecipe(PlayerInventoryWrapper playerInventory, ItemStackIngredient input, boolean useMaxItems,
          boolean allowDroppingItemsToClear, IInventorySlot inputSlot, TransactionContext transaction, AutomationType automationType) {
        ItemResource currentResource = inputSlot.resource();
        int currentStored = inputSlot.amountAsInt();
        if (input.testType(currentResource)) {
            //Input type matches
            int perRecipeAmount = input.getNeededAmount(currentResource);
            int inputCapacity = inputSlot.capacityAsInt(currentResource);
            if (inputCapacity >= perRecipeAmount) {
                int amountToMove;
                int minimumRequired;
                if (currentStored < perRecipeAmount) {
                    minimumRequired = perRecipeAmount - currentStored;
                    //Target moving until filled, or until we are at the perRecipeAmount
                    amountToMove = useMaxItems ? Math.max(0, inputCapacity - currentStored) : minimumRequired;
                } else {
                    if (!useMaxItems) {
                        //We have enough stored, and aren't trying to fill it to the maximum amount, just return that nothing needs to change
                        return PostPlaceAction.NOTHING;
                    }
                    //Try to fill the slot
                    amountToMove = Math.max(0, inputCapacity - currentStored);
                    minimumRequired = 0;
                }
                if (amountToMove > 0 && amountToMove >= minimumRequired) {
                    //If we are going to try to move stuff, see how much we can actually insert into the slot
                    try (Transaction simulation = Transaction.open(transaction)) {
                        amountToMove = inputSlot.insert(currentResource, amountToMove, simulation, automationType);
                    }
                }
                //If we have to move at least the amount needed to complete the recipe try to do so, otherwise consider that this input cannot match
                // and fall through to treating the current one as invalid
                if (amountToMove >= minimumRequired) {
                    if (amountToMove == 0) {
                        //We don't actually need to transfer anything more
                        return PostPlaceAction.NOTHING;
                    }
                    ResourceHandler<ItemResource> mainSlots = playerInventory.getMainSlots();
                    try (Transaction subTransaction = Transaction.open(transaction)) {
                        int extracted = mainSlots.extract(currentResource, amountToMove, subTransaction);
                        //If we can extract at least the amount we needed consider it valid
                        if (extracted >= minimumRequired) {
                            //If we couldn't extract anything (but were just trying to use more items),
                            // or we can insert everything we extracted into our slot
                            if (extracted == 0 || inputSlot.insert(currentResource, extracted, subTransaction, automationType) == extracted) {
                                //We succeeded in moving things to where they should be, commit the changes and return that nothing else needs to be done
                                subTransaction.commit();
                                return PostPlaceAction.NOTHING;
                            }
                        }
                    }
                }
            } else if (currentStored >= perRecipeAmount) {
                //We have enough stored, and are already fully filled. In general this will only happen if a slot size changes
                return PostPlaceAction.NOTHING;
            }
            //We can't get enough of this input stored so consider it invalid and fall through and continue trying
        }
        try (Transaction subTransaction = Transaction.open(transaction)) {
            if (!currentResource.isEmpty()) {
                if (inputSlot.extract(currentResource, currentStored, subTransaction, automationType) != currentStored || !inputSlot.isEmpty()) {
                    //If we couldn't extract it all, or if after doing so it still isn't empty, we can't handle it for this recipe:
                    // so just fallback to placing the ghost recipe as we don't require clearing the slots to render ghost recipes
                    return DEFAULT_ACTION;
                }
            }
            ResourceHandler<ItemResource> mainSlots = playerInventory.getMainSlots();
            Set<ItemResource> seenResources = new HashSet<>();
            for (int slot = 0, size = mainSlots.size(); slot < size; slot++) {
                ItemResource inventoryResource = mainSlots.getResource(slot);
                if (!inventoryResource.isEmpty() && seenResources.add(inventoryResource) && input.testType(inventoryResource)) {
                    //If it is a resource we haven't seen yet, and one that is valid as an input, try seeing if we can add it to the slot
                    int perRecipeAmount = input.getNeededAmount(inventoryResource);
                    int toExtract = useMaxItems ? inputSlot.capacityAsInt(inventoryResource) : perRecipeAmount;
                    //This also acts as an implicit toExtract > 0
                    if (toExtract >= perRecipeAmount) {
                        try (Transaction simulation = Transaction.open(subTransaction)) {
                            //See how much of the inventory resource we can actually insert
                            toExtract = inputSlot.insert(inventoryResource, toExtract, simulation, automationType);
                        }
                        //If we have room to insert the amount for at least one recipe, see how much we actually have available
                        if (toExtract >= perRecipeAmount) {
                            try (Transaction forResource = Transaction.open(subTransaction)) {
                                int extracted = mainSlots.extract(inventoryResource, toExtract, forResource);
                                if (extracted >= perRecipeAmount && inputSlot.insert(inventoryResource, extracted, forResource, automationType) == extracted) {
                                    //If we extracted enough for at least one recipe, and transferred it into the inventory slot
                                    // commit the move, and break out of trying to find the recipe input
                                    forResource.commit();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (inputSlot.isEmpty()) {
                //We failed to find anything to move into it, bail
                return DEFAULT_ACTION;
            }
            if (!currentResource.isEmpty()) {
                //At the end try moving the input back into the player's inventory
                if (allowDroppingItemsToClear) {
                    playerInventory.placeItemBackInInventory(currentResource, currentStored, subTransaction);
                } else if (mainSlots.insert(currentResource, currentStored, subTransaction) < currentStored) {
                    //Abort, we can't put the items from the input slot into the player's inventory, and we are not allowed to drop them
                    return DEFAULT_ACTION;
                }
            }
            //Success, nothing left to do. Commit the movement
            subTransaction.commit();
            return PostPlaceAction.NOTHING;
        }
    }
}