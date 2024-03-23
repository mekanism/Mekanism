package mekanism.client.recipe_viewer.interfaces;

import mekanism.client.gui.element.GuiElement;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mekanism.client.recipe_viewer.type.RecipeViewerRecipeType;
import mekanism.common.recipe.lookup.IRecipeLookupHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRecipeViewerRecipeArea<ELEMENT extends GuiElement> extends GuiEventListener {

    /**
     * @return null if not an active recipe area, otherwise the category
     */
    @Nullable
    IRecipeViewerRecipeType<?>[] getRecipeCategories();

    default boolean isRecipeViewerAreaActive() {
        return true;
    }

    ELEMENT recipeViewerCategories(@NotNull IRecipeViewerRecipeType<?>... recipeCategories);

    default ELEMENT recipeViewerCategory(IRecipeLookupHandler<?> recipeLookup) {
        IRecipeViewerRecipeType<?> recipeType = recipeLookup.recipeViewerType();
        if (recipeType != null) {
            return recipeViewerCategories(recipeType);
        }
        return (ELEMENT) this;
    }

    default ELEMENT recipeViewerCrafting() {
        return recipeViewerCategories(RecipeViewerRecipeType.VANILLA_CRAFTING);
    }

    default boolean isMouseOverRecipeViewerArea(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY);
    }
}