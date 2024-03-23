package mekanism.client.recipe_viewer.interfaces;

import java.util.Optional;
import net.minecraft.client.renderer.Rect2i;

public interface IRecipeViewerIngredientHelper {

    /**
     * Gets the ingredient under the mouse.
     *
     * @param mouseX X position of mouse.
     * @param mouseY Y position of mouse.
     *
     * @return Ingredient or {@link Optional#empty()}.
     *
     * @apiNote isMouseOver is called before this method, the positions are mainly provided for use by things like
     * {@link mekanism.client.gui.element.scroll.GuiSlotScroll} that may have different ingredients based on where in the element the mouse is.
     */
    Optional<?> getIngredient(double mouseX, double mouseY);

    /**
     * Gets the bounds of the ingredient for where it can be clicked.
     */
    Rect2i getIngredientBounds(double mouseX, double mouseY);
}