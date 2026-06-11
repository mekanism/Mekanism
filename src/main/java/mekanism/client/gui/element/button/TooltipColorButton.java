package mekanism.client.gui.element.button;

import java.util.function.BooleanSupplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.tooltip.TooltipUtils;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class TooltipColorButton extends BasicColorButton {

    private final BooleanSupplier toggled;
    @Nullable
    private final Tooltip enabled;
    @Nullable
    private final Tooltip disabled;

    public TooltipColorButton(IGuiWrapper gui, int x, int y, int size, EnumColor color, BooleanSupplier toggled, IClickable onLeftClick,
          Component enabled, Component disabled) {
        super(gui, x, y, size, () -> toggled.getAsBoolean() ? color : null, onLeftClick, onLeftClick);
        this.toggled = toggled;
        this.enabled = TooltipUtils.create(enabled);
        this.disabled = TooltipUtils.create(disabled);

    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        setTooltip(toggled.getAsBoolean() ? enabled : disabled);
    }

}