package mekanism.client.gui.element.button;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;

public class ToggleButton extends MekanismImageButton {

    private static final ResourceLocation TOGGLE = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "toggle.png");
    private static final ResourceLocation TOGGLE_FLIPPED = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "toggle_flipped.png");

    private final BooleanSupplier toggled;

    public ToggleButton(IGuiWrapper gui, int x, int y, BooleanSupplier toggled, Runnable onPress, IHoverable onHover) {
        this(gui, x, y, 18, toggled, onPress, onHover);
    }

    public ToggleButton(IGuiWrapper gui, int x, int y, int size, BooleanSupplier toggled, Runnable onPress, IHoverable onHover) {
        super(gui, x, y, size, 18, TOGGLE, onPress, onHover);
        this.toggled = toggled;
    }

    @Override
    protected ResourceLocation getResource() {
        return toggled.getAsBoolean() ? TOGGLE_FLIPPED : super.getResource();
    }
}