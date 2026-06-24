package mekanism.client.gui.element;

import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiDigitalSwitch.SwitchType;
import net.minecraft.network.chat.Component;

public class GuiScreenSwitch extends GuiInnerScreen {

    private final GuiDigitalSwitch digitalSwitch;

    public GuiScreenSwitch(IGuiWrapper gui, int x, int y, int width, Component buttonName, BooleanSupplier stateSupplier, IClickable onToggle) {
        List<Component> text = Collections.singletonList(buttonName);
        super(gui, x, y, width, GuiDigitalSwitch.BUTTON_SIZE_Y * 2 + 6, () -> text);
        this.active = true;
        padding(4);
        digitalSwitch = addChild(new GuiDigitalSwitch(gui, x + this.width - 2 - GuiDigitalSwitch.BUTTON_SIZE_X, y + 2, null, stateSupplier, onToggle, SwitchType.LOWER_ICON));
    }

    @Override
    protected int getMaxTextWidth(int row) {
        return super.getMaxTextWidth(row) - 2 - digitalSwitch.getWidth();
    }
}
