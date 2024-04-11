package mekanism.client.gui.element.button;

import java.util.function.BooleanSupplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.tooltip.TooltipUtils;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class TooltipColorButton extends BasicColorButton {

    private final BooleanSupplier toggled;
    private final Tooltip enabled;
    private final Tooltip disabled;

    public TooltipColorButton(IGuiWrapper gui, int x, int y, int size, EnumColor color, BooleanSupplier toggled, @NotNull IClickable onLeftClick,
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