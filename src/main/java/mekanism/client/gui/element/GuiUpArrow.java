package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import net.minecraft.resources.Identifier;

public class GuiUpArrow extends GuiTextureOnlyElement {

    private static final Identifier ARROW = Mekanism.rl("arrow/up");

    public GuiUpArrow(IGuiWrapper gui, int x, int y) {
        super(ARROW, gui, x, y, 8, 10);
    }
}