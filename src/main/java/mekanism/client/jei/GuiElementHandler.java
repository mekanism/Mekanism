package mekanism.client.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.client.jei.interfaces.IJEIIngredientHelper;
import mekanism.client.jei.interfaces.IJEIRecipeArea;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.ResourceLocation;

public class GuiElementHandler implements IGuiContainerHandler<GuiMekanism<?>> {

    private static boolean areaSticksOut(int x, int y, int width, int height, int parentX, int parentY, int parentWidth, int parentHeight) {
        return x < parentX || y < parentY || x + width > parentX + parentWidth || y + height > parentY + parentHeight;
    }

    public static List<Rectangle2d> getAreasFor(int parentX, int parentY, int parentWidth, int parentHeight, Collection<? extends IGuiEventListener> children) {
        List<Rectangle2d> areas = new ArrayList<>();
        for (IGuiEventListener child : children) {
            if (child instanceof Widget) {
                Widget widget = (Widget) child;
                if (widget.visible) {
                    if (areaSticksOut(widget.x, widget.y, widget.getWidth(), widget.getHeight(), parentX, parentY, parentWidth, parentHeight)) {
                        //If the element sticks out at all add it
                        areas.add(new Rectangle2d(widget.x, widget.y, widget.getWidth(), widget.getHeight()));
                    }
                    if (widget instanceof GuiElement) {
                        //Start by getting all the areas in the grandchild that stick outside the child in theory this should cut down
                        // on our checks a fair bit as most children will fully contain all their grandchildren
                        for (Rectangle2d grandChildArea : getAreasFor(widget.x, widget.y, widget.getWidth(), widget.getHeight(), ((GuiElement) widget).children())) {
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
        }
        return areas;
    }

    @Override
    public List<Rectangle2d> getGuiExtraAreas(GuiMekanism<?> gui) {
        int parentX = gui.getLeft();
        int parentY = gui.getTop();
        int parentWidth = gui.getWidth();
        int parentHeight = gui.getHeight();
        //Add any children the gui has and any windows the gui has including all grandchildren that poke out of the main gui
        List<Rectangle2d> extraAreas = getAreasFor(parentX, parentY, parentWidth, parentHeight, gui.children());
        extraAreas.addAll(getAreasFor(parentX, parentY, parentWidth, parentHeight, gui.getWindows()));
        return extraAreas;
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(GuiMekanism<?> gui, double mouseX, double mouseY) {
        GuiWindow guiWindow = gui.getWindowHovering(mouseX, mouseY);
        //If no window is being hovered, then check the elements in general; otherwise, check the elements of the window
        return getIngredientUnderMouse(guiWindow == null ? gui.children() : guiWindow.children(), mouseX, mouseY);
    }

    @Nullable
    private Object getIngredientUnderMouse(List<? extends IGuiEventListener> children, double mouseX, double mouseY) {
        for (IGuiEventListener child : children) {
            if (child instanceof Widget) {
                Widget widget = (Widget) child;
                if (!widget.visible) {
                    //Skip this child if it isn't visible
                    continue;
                }
                if (widget instanceof GuiElement) {
                    GuiElement element = (GuiElement) widget;
                    //Start by checking if we have any grandchildren that have an element being hovered as if we do it is the one
                    // we want to take as the grandchildren in general should be a more "forward" facing layer
                    Object underGrandChild = getIngredientUnderMouse(element.children(), mouseX, mouseY);
                    if (underGrandChild != null) {
                        //If we have a grandchild that was an ingredient helper, return its ingredient
                        return underGrandChild;
                    }
                }
            }
            if (child instanceof IJEIIngredientHelper && child.isMouseOver(mouseX, mouseY)) {
                return ((IJEIIngredientHelper) child).getIngredient(mouseX, mouseY);
            }
        }
        return null;
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

    private Collection<IGuiClickableArea> getGuiClickableArea(List<? extends IGuiEventListener> children, double mouseX, double mouseY) {
        for (IGuiEventListener child : children) {
            if (child instanceof GuiElement) {
                GuiElement element = (GuiElement) child;
                if (element.visible) {
                    //Start by checking if any of the grandchildren are JEI clickable areas that can be used
                    // as we want to start with the "top" layer
                    Collection<IGuiClickableArea> clickableGrandChildAreas = getGuiClickableArea(element.children(), mouseX, mouseY);
                    if (!clickableGrandChildAreas.isEmpty()) {
                        return clickableGrandChildAreas;
                    }
                    //If we couldn't find any, then we need to continue on to checking this element itself
                    if (element instanceof IJEIRecipeArea) {
                        IJEIRecipeArea<?> recipeArea = (IJEIRecipeArea<?>) element;
                        if (recipeArea.isActive()) {
                            ResourceLocation[] categories = recipeArea.getRecipeCategories();
                            //getRecipeCategory is a cheaper call than isMouseOver, so we perform it first
                            if (categories != null && recipeArea.isMouseOverJEIArea(mouseX, mouseY)) {
                                //TODO: Decide if we want our own implementation to overwrite the getTooltipStrings and have it show something like "Crusher Recipes"
                                IGuiClickableArea clickableArea = IGuiClickableArea.createBasic(element.getRelativeX(), element.getRelativeY(),
                                      element.getWidth(), element.getHeight(), categories);
                                return Collections.singleton(clickableArea);
                            }
                        }
                    }
                }
            }
        }
        return Collections.emptyList();
    }
}