package mekanism.client.recipe_viewer.jei.transfer;

import java.util.List;
import mekanism.api.AutomationType;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.recipes.SingleInputRecipe.ItemInputRecipe;
import mekanism.api.resource.IResourceContainer;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.common.Mekanism;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.lib.recipetransfer.PlaceMekanismRecipe;
import mekanism.common.recipe.lookup.IRecipeLookupHandler.RecipeContainers;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class SimpleMachineTransferHandler<CONTAINER extends MekanismContainer, RECIPE extends ItemInputRecipe<?, ?, ?>> extends JeiMachineTransferHandler<CONTAINER, RECIPE> {

    public SimpleMachineTransferHandler(IRecipeTransferHandlerHelper transferHelper, Class<CONTAINER> containerClass, MenuType<CONTAINER> menuType, IRecipeViewerRecipeType<RECIPE> recipeTypeProvider) {
        super(transferHelper, containerClass, menuType, recipeTypeProvider);
    }

    @Override
    protected TransferResult transferRecipe(RecipeContainers recipeContainers, RECIPE recipe, JeiPlacementHelper helper, TransactionContext transaction,
                                            AutomationType automationType) {
        List<? extends IResourceContainer<?>> inputs = recipeContainers.inputs();
        if (inputs.size() == 1 && inputs.getFirst() instanceof IInventorySlot inputSlot) {
            return PlaceMekanismRecipe.placeRecipe(helper, recipe.getInput(), inputSlot, transaction, automationType);
        }
        Mekanism.logger.error("Encountered ItemInputRecipe recipe via JEI Transfer but recipe containers were not as expected did not match");
        return TransferResult.INTERNAL_ERROR;
    }
}