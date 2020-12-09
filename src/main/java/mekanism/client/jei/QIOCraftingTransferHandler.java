package mekanism.client.jei;

import javax.annotation.Nullable;
import mekanism.common.inventory.container.QIOItemViewerContainer;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.PlayerEntity;

public class QIOCraftingTransferHandler implements IRecipeTransferHandler<QIOItemViewerContainer> {

    private final IRecipeTransferHandlerHelper handlerHelper;

    public QIOCraftingTransferHandler(IRecipeTransferHandlerHelper handlerHelper) {
        this.handlerHelper = handlerHelper;
    }

    @Override
    public Class<QIOItemViewerContainer> getContainerClass() {
        return QIOItemViewerContainer.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(QIOItemViewerContainer container, Object recipe, IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer,
          boolean doTransfer) {
        if (container.getSelectedCraftingGrid() == -1) {
            //Note: While the java docs recommend to log a message to the console when returning an internal error,
            // this isn't actually an error state here, and is just one where we want to make sure the plus button is hidden
            // as there are no crafting grids being shown
            return this.handlerHelper.createInternalError();
        }

        //TODO: Implement me, start by creating errors that show which crafting grid number the player has selected?

        //TODO: Do we need to validate the user has access?? Aka if they are looking at JEI does our kick out code on
        // security level change actually work properly??

        //TODO: Validate recipe -> missing items -> createUserErrorForSlots "jei.tooltip.error.recipe.transfer.missing")

        if (doTransfer) {
            //TODO: Send packet to server with information of what to transfer and where
        }
        //TODO: Switch this back to returning null, this for now is mainly for debug purposes
        return this.handlerHelper.createUserErrorWithTooltip("QIO Crafting Grid " + container.getSelectedCraftingGrid() + " selected.");
    }
}