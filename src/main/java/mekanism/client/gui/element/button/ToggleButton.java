package mekanism.client.gui.element.button;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.Mekanism;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class ToggleButton extends MekanismImageButton {

    private static final Identifier TOGGLE = Mekanism.rl("button/toggle");
    private static final Identifier TOGGLE_FLIPPED = Mekanism.rl("button/toggle_flipped");

    private final Identifier flipped;
    private final BooleanSupplier toggled;
    @Nullable
    private final Tooltip yes;
    @Nullable
    private final Tooltip no;

    public ToggleButton(IGuiWrapper gui, int x, int y, BooleanSupplier toggled, IClickable onPress) {
        this(gui, x, y, 18, toggled, onPress);
    }

    public ToggleButton(IGuiWrapper gui, int x, int y, int size, BooleanSupplier toggled, IClickable onPress) {
        this(gui, x, y, size, size, TOGGLE, TOGGLE_FLIPPED, toggled, onPress, null, null);
    }

    public ToggleButton(IGuiWrapper gui, int x, int y, int width, int height, Identifier toggle, Identifier flipped,
          BooleanSupplier toggled, IClickable onPress, @Nullable Component yes, @Nullable Component no) {
        super(gui, x, y, width, height, toggle, onPress);
        this.toggled = toggled;
        this.flipped = flipped;
        this.yes = TooltipUtils.create(yes);
        this.no = TooltipUtils.create(no);
    }

    @Override
    protected Identifier getResource() {
        return toggled.getAsBoolean() ? flipped : super.getResource();
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        setTooltip(toggled.getAsBoolean() ? yes : no);
    }
}