package mekanism.client.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiTexturedElement;
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
            if (child instanceof GuiTexturedElement) {
                GuiTexturedElement element = (GuiTexturedElement) child;
                //TODO: Only do this if it goes past the border
                extraAreas.add(new Rectangle2d(element.x, element.y, element.getWidth(), element.getHeight()));
            }
        }
        return extraAreas;
    }

    @Override
    public Object getIngredientUnderMouse(GuiMekanism gui, double mouseX, double mouseY) {
        return null;
    }

    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(GuiMekanism gui) {
        //TODO: Evaluate, maybe we want to make this be all GuiProgress elements
        return Collections.emptyList();
    }
}