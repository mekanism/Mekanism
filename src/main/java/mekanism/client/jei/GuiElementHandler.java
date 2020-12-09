package mekanism.client.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiRelativeElement;
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

    private static void addAreaIfOutside(List<Rectangle2d> areas, int parentX, int parentY, int parentWidth, int parentHeight, Widget element) {
        if (element.visible) {
            int x = element.x;
            int y = element.y;
            int width = element.getWidth();
            int height = element.getHeightRealms();
            if (x < parentX || y < parentY || x + width > parentX + parentWidth || y + height > parentY + parentHeight) {
                //If the element sticks out at all add it
                areas.add(new Rectangle2d(x, y, width, height));
            }
        }
    }

    public static List<Rectangle2d> getAreasFor(int parentX, int parentY, int parentWidth, int parentHeight, List<? extends IGuiEventListener> children) {
        List<Rectangle2d> areas = new ArrayList<>();
        for (IGuiEventListener child : children) {
            if (child instanceof Widget) {
                addAreaIfOutside(areas, parentX, parentY, parentWidth, parentHeight, (Widget) child);
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
        List<Rectangle2d> extraAreas = getAreasFor(parentX, parentY, parentWidth, parentHeight, gui.children());
        for (GuiWindow window : gui.getWindows()) {
            //Add the window itself and any areas that poke out from the main gui
            addAreaIfOutside(extraAreas, parentX, parentY, parentWidth, parentHeight, window);
            extraAreas.addAll(getAreasFor(parentX, parentY, parentWidth, parentHeight, window.children()));
        }
        return extraAreas;
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(GuiMekanism<?> gui, double mouseX, double mouseY) {
        GuiWindow guiWindow = gui.getWindowHovering(mouseX, mouseY);
        if (guiWindow == null) {
            //If no window is being hovered, then check the elements in general
            return getIngredientUnderMouse(gui.children(), mouseX, mouseY);
        }
        //Otherwise check the elements of the window
        return getIngredientUnderMouse(guiWindow.children(), mouseX, mouseY);
    }

    @Nullable
    private Object getIngredientUnderMouse(List<? extends IGuiEventListener> children, double mouseX, double mouseY) {
        for (IGuiEventListener child : children) {
            if (child instanceof IJEIIngredientHelper && child.isMouseOver(mouseX, mouseY)) {
                return ((IJEIIngredientHelper) child).getIngredient();
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
        //Otherwise check the elements of the window
        return getGuiClickableArea(guiWindow.children(), mouseX, mouseY);
    }

    private Collection<IGuiClickableArea> getGuiClickableArea(List<? extends IGuiEventListener> children, double mouseX, double mouseY) {
        for (IGuiEventListener child : children) {
            if (child instanceof GuiRelativeElement && child instanceof IJEIRecipeArea) {
                IJEIRecipeArea<?> recipeArea = (IJEIRecipeArea<?>) child;
                if (recipeArea.isActive()) {
                    ResourceLocation[] categories = recipeArea.getRecipeCategories();
                    //getRecipeCategory is a cheaper call than isMouseOver so we perform it first
                    if (categories != null && recipeArea.isMouseOverJEIArea(mouseX, mouseY)) {
                        GuiRelativeElement element = (GuiRelativeElement) child;
                        //TODO: Decide if we want our own implementation to overwrite the getTooltipStrings and have it show something like "Crusher Recipes"
                        IGuiClickableArea clickableArea = IGuiClickableArea.createBasic(element.getRelativeX(), element.getRelativeY(), element.getWidth(),
                              element.getHeightRealms(), categories);
                        return Collections.singleton(clickableArea);
                    }
                }
            }
        }
        return Collections.emptyList();
    }
}