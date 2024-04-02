package mekanism.client.recipe_viewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.text.GuiTextField;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerIngredientHelper;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;

public class GuiElementHandler {

    private static boolean areaSticksOut(int x, int y, int width, int height, int parentX, int parentY, int parentWidth, int parentHeight) {
        return x < parentX || y < parentY || x + width > parentX + parentWidth || y + height > parentY + parentHeight;
    }

    public static List<Rect2i> getAreasFor(int parentX, int parentY, int parentWidth, int parentHeight, Collection<? extends GuiEventListener> children) {
        List<Rect2i> areas = new ArrayList<>();
        for (GuiEventListener child : children) {
            if (child instanceof AbstractWidget widget && widget.visible) {
                if (areaSticksOut(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), parentX, parentY, parentWidth, parentHeight)) {
                    //If the element sticks out at all add it
                    areas.add(new Rect2i(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight()));
                }
                if (widget instanceof ContainerEventHandler eventHandler) {
                    //Start by getting all the areas in the grandchild that stick outside the child in theory this should cut down
                    // on our checks a fair bit as most children will fully contain all their grandchildren
                    for (Rect2i grandChildArea : getAreasFor(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight(), eventHandler.children())) {
                        //Then check if that area that is sticking outside the child sticks outside the parent as well
                        if (areaSticksOut(grandChildArea.getX(), grandChildArea.getY(), grandChildArea.getWidth(), grandChildArea.getHeight(),
                              parentX, parentY, parentWidth, parentHeight)) {
                            //If it does, then add it to our areas
                            areas.add(grandChildArea);
                        }
                    }
                }
            }
        }
        return areas;
    }

    public static List<Rect2i> getGuiExtraAreas(GuiMekanism<?> gui) {
        int parentX = gui.getGuiLeft();
        int parentY = gui.getGuiTop();
        int parentWidth = gui.getXSize();
        int parentHeight = gui.getYSize();
        //Add any children the gui has and any windows the gui has including all grandchildren that poke out of the main gui
        List<Rect2i> extraAreas = getAreasFor(parentX, parentY, parentWidth, parentHeight, gui.children());
        extraAreas.addAll(getAreasFor(parentX, parentY, parentWidth, parentHeight, gui.getWindows()));
        return extraAreas;
    }

    public static <INGREDIENT> Optional<INGREDIENT> getClickableIngredientUnderMouse(GuiMekanism<?> gui, double mouseX, double mouseY,
          BiFunction<IRecipeViewerIngredientHelper, Object, INGREDIENT> ingredientWrapper) {
        GuiEventListener focused = gui.getFocused();
        if (focused instanceof GuiTextField || focused instanceof EditBox) {
            //Don't mark ingredients as clickable if a text box is focused
            return Optional.empty();
        } else if (focused instanceof GuiElement element && element.getFocused() instanceof GuiTextField) {
            //Don't mark ingredients as clickable if a text box is focused
            return Optional.empty();
        }
        GuiWindow guiWindow = gui.getWindowHovering(mouseX, mouseY);
        //If no window is being hovered, then check the elements in general; otherwise, check the elements of the window
        return getIngredientUnderMouse(guiWindow == null ? gui.children() : guiWindow.children(), mouseX, mouseY, ingredientWrapper);
    }

    private static <INGREDIENT> Optional<INGREDIENT> getIngredientUnderMouse(List<? extends GuiEventListener> children, double mouseX, double mouseY,
          BiFunction<IRecipeViewerIngredientHelper, Object, INGREDIENT> ingredientWrapper) {
        for (GuiEventListener child : children) {
            if (child instanceof AbstractWidget widget) {
                if (!widget.visible) {
                    //Skip this child if it isn't visible
                    continue;
                }
                if (widget instanceof ContainerEventHandler eventHandler) {
                    //Start by checking if we have any grandchildren that have an element being hovered as if we do it is the one
                    // we want to take as the grandchildren in general should be a more "forward" facing layer
                    Optional<INGREDIENT> underGrandChild = getIngredientUnderMouse(eventHandler.children(), mouseX, mouseY, ingredientWrapper);
                    if (underGrandChild.isPresent()) {
                        //If we have a grandchild that was an ingredient helper, return its ingredient
                        return underGrandChild;
                    }
                }
            }
            //Note: We do not need to check if there is a window over the child as if we are currently hovering any window
            // we only check the children that are part of that window
            if (child instanceof IRecipeViewerIngredientHelper helper && child.isMouseOver(mouseX, mouseY)) {
                Optional<?> ingredient = helper.getIngredient(mouseX, mouseY);
                //noinspection OptionalIsPresent - Capturing lambda
                if (ingredient.isPresent()) {
                    return Optional.ofNullable(ingredientWrapper.apply(helper, ingredient.get()));
                }
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}