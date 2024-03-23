package mekanism.client.recipe_viewer.jei;

import java.util.List;
import java.util.Optional;
import mekanism.client.recipe_viewer.RVTransferUtils;
import mekanism.common.inventory.container.tile.FormulaicAssemblicatorContainer;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class FormulaicRecipeTransferInfo implements IRecipeTransferInfo<FormulaicAssemblicatorContainer, RecipeHolder<CraftingRecipe>> {

    @Override
    public Class<FormulaicAssemblicatorContainer> getContainerClass() {
        return FormulaicAssemblicatorContainer.class;
    }

    @Override
    public Optional<MenuType<FormulaicAssemblicatorContainer>> getMenuType() {
        return Optional.empty();
    }

    @Override
    public RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }

    @Override
    public boolean canHandle(FormulaicAssemblicatorContainer container, RecipeHolder<CraftingRecipe> recipe) {
        return true;
    }

    @Override
    public List<Slot> getRecipeSlots(FormulaicAssemblicatorContainer container, RecipeHolder<CraftingRecipe> recipe) {
        return RVTransferUtils.getFormulaicCraftingSlots(container);
    }

    @Override
    public List<Slot> getInventorySlots(FormulaicAssemblicatorContainer container, RecipeHolder<CraftingRecipe> recipe) {
        return RVTransferUtils.getFormulaicInputSlots(container);
    }
}