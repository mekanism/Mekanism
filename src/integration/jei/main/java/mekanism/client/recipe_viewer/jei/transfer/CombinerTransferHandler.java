package mekanism.client.recipe_viewer.jei.transfer;

import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.resource.IResourceContainer;
import mekanism.client.recipe_viewer.jei.RecipeRegistryHelper;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.tile.MekanismTileContainer;
import mekanism.common.lib.recipetransfer.PlaceMekanismRecipe;
import mekanism.common.recipe.lookup.IRecipeLookupHandler.RecipeContainers;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.tile.machine.TileEntityCombiner;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class CombinerTransferHandler extends JeiMachineTransferHandler<MekanismTileContainer<TileEntityCombiner>, CombinerRecipe> {

    public CombinerTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
        super(transferHelper, RecipeRegistryHelper.tileContainerClass(), MekanismContainerTypes.COMBINER.value(), RecipeViewerRecipeType.COMBINING);
    }

    @Override
    protected TransferResult transferRecipe(RecipeContainers recipeContainers, CombinerRecipe recipe, JeiPlacementHelper helper, TransactionContext transaction,
											AutomationType automationType) {
        List<? extends IResourceContainer<?>> inputs = recipeContainers.inputs();
        if (inputs.size() == 2 && inputs.getFirst() instanceof IInventorySlot inputSlot && inputs.getLast() instanceof IInventorySlot secondaryInput) {
            return PlaceMekanismRecipe.placeRecipe(helper, recipe.getMainInput(), inputSlot, recipe.getExtraInput(), secondaryInput, transaction, automationType);
        }
        Mekanism.logger.error("Encountered CombinerRecipe recipe via JEI Transfer but recipe containers were not as expected did not match");
        return TransferResult.INTERNAL_ERROR;
    }
}