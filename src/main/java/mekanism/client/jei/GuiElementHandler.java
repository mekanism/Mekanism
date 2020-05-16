package mekanism.client.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.element.GuiRelativeElement;
import mekanism.client.gui.element.GuiWindow;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.Rectangle2d;

public class GuiElementHandler implements IGuiContainerHandler<GuiMekanism> {

    @Override
    public List<Rectangle2d> getGuiExtraAreas(GuiMekanism gui) {
        List<Rectangle2d> extraAreas = new ArrayList<>();
        List<? extends IGuiEventListener> children = gui.children();
        for (IGuiEventListener child : children) {
            //TODO: Decide if we just want to do this for any GuiElement
            if (child instanceof GuiRelativeElement) {
                GuiRelativeElement element = (GuiRelativeElement) child;
                extraAreas.add(new Rectangle2d(element.x, element.y, element.getWidth(), element.getHeight()));
            }
        }
        for (GuiWindow window : (Collection<GuiWindow>) gui.getWindows()) {
            extraAreas.add(new Rectangle2d(window.x, window.y, window.getWidth(), window.getHeight()));
            for (GuiElement element : window.children()) {
                if (element instanceof GuiRelativeElement) {
                    extraAreas.add(new Rectangle2d(element.x, element.y, element.getWidth(), element.getHeight()));
                }
            }
        }
        return extraAreas;
    }

    @Override
    public Object getIngredientUnderMouse(GuiMekanism gui, double mouseX, double mouseY) {
        return gui.children().stream().filter(element -> element instanceof IJEIIngredientHelper && element.isMouseOver(mouseX, mouseY))
              .findFirst().map(element -> ((IJEIIngredientHelper) element).getIngredient()).orElse(null);
    }

    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(GuiMekanism gui) {
        //TODO: Create a flag for GuiProgress elements that allows for them to know they are for JEI
        return Collections.emptyList();
    }
}