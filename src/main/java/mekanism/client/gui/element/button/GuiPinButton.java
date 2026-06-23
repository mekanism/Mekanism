package mekanism.client.gui.element.button;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiWindow;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import net.minecraft.resources.Identifier;

public class GuiPinButton extends ToggleButton {

    private static final Identifier PINNED = Mekanism.rl("button/pinned");
    private static final Identifier UNPINNED = Mekanism.rl("button/unpinned");
    public static final int WIDTH = 16;

    public GuiPinButton(IGuiWrapper gui, int x, int y, GuiWindow window) {
        super(gui, x, y, WIDTH, 8, UNPINNED, PINNED, window::isPinned, window::togglePinned, MekanismLang.UNPIN.translate(), MekanismLang.PIN.translate());
    }
}
