package mekanism.client.jei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.gui.element.GuiElement;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import net.minecraft.client.gui.inventory.GuiContainer;

public class GuiElementHandler implements IAdvancedGuiHandler {

    @Override
    public Class getGuiContainerClass() {
        return GuiMekanism.class;
    }

    @Override
    public List<Rectangle> getGuiExtraAreas(GuiContainer gui) {
        if (gui instanceof GuiMekanism) {
            List<Rectangle> list = new ArrayList<>();

            GuiMekanism guiMek = (GuiMekanism) gui;

            for (GuiElement element : guiMek.guiElements) {
                list.add(element.getBounds(guiMek.getXPos(), guiMek.getYPos()).toRectangle());
            }

            return list;
        }

        return null;
    }

    @Override
    public Object getIngredientUnderMouse(GuiContainer guiContainer, int mouseX, int mouseY) {
        return null;
    }
}
