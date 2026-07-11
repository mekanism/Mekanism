package mekanism.client.recipe_viewer.emi.transfer;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import java.util.List;
import mekanism.client.recipe_viewer.RVTransferUtils;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import net.minecraft.world.inventory.Slot;

public class FormulaicAssemblicatorTransferHandler implements StandardRecipeHandler<FormulaicAssemblicatorContainer> {

    @Override
    public List<Slot> getInputSources(FormulaicAssemblicatorContainer container) {
        return RVTransferUtils.getFormulaicInputSlots(container);
    }

    @Override
    public List<Slot> getCraftingSlots(FormulaicAssemblicatorContainer container) {
        return RVTransferUtils.getFormulaicCraftingSlots(container);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree();
    }
}