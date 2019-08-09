package mekanism.client.jei;

import java.awt.Rectangle;
import java.util.List;
import java.util.stream.Collectors;
import mekanism.client.gui.GuiMekanism;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;

public class GuiElementHandler implements IAdvancedGuiHandler {

    @Override
    public Class getGuiContainerClass() {
        return GuiMekanism.class;
    }

    @Override
    public List<Rectangle> getGuiExtraAreas(ContainerScreen gui) {
        if (gui instanceof GuiMekanism) {
            GuiMekanism guiMek = (GuiMekanism) gui;
            return guiMek.getGuiElements().stream().map(element -> element.getBounds(guiMek.getGuiLeft(), guiMek.getGuiTop()).toRectangle()).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public Object getIngredientUnderMouse(ContainerScreen guiContainer, int mouseX, int mouseY) {
        return null;
    }
}