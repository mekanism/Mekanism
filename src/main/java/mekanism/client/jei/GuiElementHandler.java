package mekanism.client.jei;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiRelativeElement;
import mekanism.client.gui.element.GuiWindow;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.ResourceLocation;

public class GuiElementHandler implements IGuiContainerHandler<GuiMekanism> {

    @Override
    public List<Rectangle2d> getGuiExtraAreas(GuiMekanism genericGui) {
        GuiMekanism<?> gui = (GuiMekanism<?>) genericGui;
        List<Rectangle2d> extraAreas = new ArrayList<>();
        List<? extends IGuiEventListener> children = gui.children();
        for (IGuiEventListener child : children) {
            //TODO: Decide if we just want to do this for any GuiElement
            if (child instanceof GuiRelativeElement) {
                GuiRelativeElement element = (GuiRelativeElement) child;
                extraAreas.add(new Rectangle2d(element.x, element.y, element.getWidth(), element.getHeight()));
            }
        }
        for (GuiWindow window : gui.getWindows()) {
            extraAreas.add(new Rectangle2d(window.x, window.y, window.getWidth(), window.getHeight()));
            for (GuiElement element : window.children()) {
                if (element instanceof GuiRelativeElement) {
                    extraAreas.add(new Rectangle2d(element.x, element.y, element.getWidth(), element.getHeight()));
                }
            }
        }
        return extraAreas;
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(GuiMekanism genericGui, double mouseX, double mouseY) {
        GuiMekanism<?> gui = (GuiMekanism<?>) genericGui;
        //Check the "active" gui window first
        for (GuiWindow guiWindow : gui.getWindows()) {
            if (guiWindow.isMouseOver(mouseX, mouseY)) {
                return getIngredientUnderMouse(guiWindow.children(), mouseX, mouseY);
            }
        }
        //If none are hovered, then check the elements in general
        return getIngredientUnderMouse(gui.children(), mouseX, mouseY);
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

    @Nullable
    //@Override//TODO - V10: Uncomment htis override after updating to new JEI
    public IGuiClickableArea getGuiClickableArea(GuiMekanism genericGui, double mouseX, double mouseY) {
        GuiMekanism<?> gui = (GuiMekanism<?>) genericGui;
        //Make mouseX and mouseY not be relative
        mouseX += gui.getGuiLeft();
        mouseY += gui.getGuiTop();
        //Check the "active" gui window first
        for (GuiWindow guiWindow : gui.getWindows()) {
            if (guiWindow.isMouseOver(mouseX, mouseY)) {
                return getGuiClickableArea(guiWindow.children(), mouseX, mouseY);
            }
        }
        //If none are hovered, then check the elements in general
        return getGuiClickableArea(gui.children(), mouseX, mouseY);
    }

    @Nullable
    private IGuiClickableArea getGuiClickableArea(List<? extends IGuiEventListener> children, double mouseX, double mouseY) {
        for (IGuiEventListener child : children) {
            if (child instanceof GuiRelativeElement && child instanceof IJEIRecipeArea) {
                IJEIRecipeArea<?> recipeArea = (IJEIRecipeArea<?>) child;
                if (recipeArea.isActive()) {
                    ResourceLocation[] categories = recipeArea.getRecipeCategories();
                    //getRecipeCategory is a cheaper call than isMouseOver so we perform it first
                    if (categories != null && child.isMouseOver(mouseX, mouseY)) {
                        GuiRelativeElement element = (GuiRelativeElement) child;
                        //TODO: Decide if we want our own implementation to overwrite the getTooltipStrings and have it show something like "Crusher Recipes"
                        return IGuiClickableArea.createBasic(element.getRelativeX(), element.getRelativeY(), element.getWidth(), element.getHeight(), categories);
                    }
                }
            }
        }
        return null;
    }
}