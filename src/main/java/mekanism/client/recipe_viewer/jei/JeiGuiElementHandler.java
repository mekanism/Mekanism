package mekanism.client.recipe_viewer.jei;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.recipe_viewer.GuiElementHandler;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerRecipeArea;
import mekanism.client.recipe_viewer.type.IRecipeViewerRecipeType;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.runtime.IClickableIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;

public class JeiGuiElementHandler implements IGuiContainerHandler<GuiMekanism<?>> {

    private final IIngredientManager ingredientManager;

    public JeiGuiElementHandler(IIngredientManager ingredientManager) {
        this.ingredientManager = ingredientManager;
    }

    @Override
    public List<Rect2i> getGuiExtraAreas(GuiMekanism<?> gui) {
        return GuiElementHandler.getGuiExtraAreas(gui);
    }

    @Override
    public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(GuiMekanism<?> gui, double mouseX, double mouseY) {
        return GuiElementHandler.getClickableIngredientUnderMouse(gui, mouseX, mouseY, (helper, ingredient) ->
              ingredientManager.createClickableIngredient(ingredient, helper.getIngredientBounds(mouseX, mouseY), false).orElse(null));
    }

    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(GuiMekanism<?> gui, double mouseX, double mouseY) {
        //Make mouseX and mouseY not be relative
        mouseX += gui.getGuiLeft();
        mouseY += gui.getGuiTop();
        GuiWindow guiWindow = gui.getWindowHovering(mouseX, mouseY);
        if (guiWindow == null) {
            //If no window is being hovered, then check the elements in general
            return getGuiClickableArea(gui.children(), mouseX, mouseY);
        }
        //Otherwise, check the elements of the window
        return getGuiClickableArea(guiWindow.children(), mouseX, mouseY);
    }

    private Collection<IGuiClickableArea> getGuiClickableArea(List<? extends GuiEventListener> children, double mouseX, double mouseY) {
        for (GuiEventListener child : children) {
            if (child instanceof ContainerEventHandler eventHandler) {
                //Start by checking if any of the grandchildren are JEI clickable areas that can be used
                // as we want to start with the "top" layer
                Collection<IGuiClickableArea> clickableGrandChildAreas = getGuiClickableArea(eventHandler.children(), mouseX, mouseY);
                if (!clickableGrandChildAreas.isEmpty()) {
                    return clickableGrandChildAreas;
                }
                //If we couldn't find any, then we need to continue on to checking this element itself
                if (child instanceof IRecipeViewerRecipeArea<?> recipeArea && recipeArea.isRecipeViewerAreaActive() && child instanceof GuiElement element && element.visible) {
                    IRecipeViewerRecipeType<?>[] categories = recipeArea.getRecipeCategories();
                    //getRecipeCategories is a cheaper call than isMouseOver, so we perform it first
                    //Note: We do not need to check if there is a window over the child as if we are currently hovering any window
                    // we only check the children that are part of that window
                    if (categories != null && recipeArea.isMouseOverRecipeViewerArea(mouseX, mouseY)) {
                        //TODO: Decide if we want our own implementation to overwrite the getTooltip and have it show something like "Crusher Recipes"
                        IGuiClickableArea clickableArea = IGuiClickableArea.createBasic(element.getRelativeX(), element.getRelativeY(),
                              element.getWidth(), element.getHeight(), MekanismJEI.recipeType(categories));
                        return Collections.singleton(clickableArea);
                    }
                }
            }
        }
        return Collections.emptyList();
    }
}