package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import net.minecraft.resources.Identifier;

public class GuiDownArrow extends GuiTextureOnlyElement {

    private static final Identifier ARROW = Mekanism.rl("arrow/down");

    public GuiDownArrow(IGuiWrapper gui, int x, int y) {
        super(ARROW, gui, x, y, 8, 9);
    }
}