package mekanism.client.gui.element.button;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.tooltip.TooltipUtils;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class TooltipToggleButton extends MekanismImageButton {

    private final BooleanSupplier isToggled;
    @Nullable
    private final Tooltip yes;
    @Nullable
    private final Tooltip no;

    public TooltipToggleButton(IGuiWrapper gui, int x, int y, int size, Identifier resource, BooleanSupplier isToggled, IClickable onPress,
          Component yes, Component no) {
        super(gui, x, y, size, resource, onPress);
        this.isToggled = isToggled;
        this.yes = TooltipUtils.create(yes);
        this.no = TooltipUtils.create(no);
    }

    public TooltipToggleButton(IGuiWrapper gui, int x, int y, int size, Identifier resource, BooleanSupplier isToggled, IClickable onLeftClick, IClickable onRightClick,
          @Nullable Tooltip yes, @Nullable Tooltip no) {
        this(gui, x, y, size, size, resource, isToggled, onLeftClick, onRightClick, yes, no);
    }

    public TooltipToggleButton(IGuiWrapper gui, int x, int y, int size, int textureSize, Identifier resource, BooleanSupplier isToggled, IClickable onLeftClick,
          IClickable onRightClick, @Nullable Tooltip yes, @Nullable Tooltip no) {
        this(gui, x, y, size, size, textureSize, textureSize, resource, isToggled, onLeftClick, onRightClick, yes, no);
    }

    public TooltipToggleButton(IGuiWrapper gui, int x, int y, int size, int textureSize, Identifier resource, BooleanSupplier isToggled, IClickable onPress,
          Component yes, Component no) {
        this(gui, x, y, size, textureSize, resource, isToggled, onPress, TooltipUtils.create(yes), TooltipUtils.create(no));
    }

    public TooltipToggleButton(IGuiWrapper gui, int x, int y, int size, int textureSize, Identifier resource, BooleanSupplier isToggled, IClickable onPress,
          @Nullable Tooltip yes, @Nullable Tooltip no) {
        this(gui, x, y, size, size, textureSize, textureSize, resource, isToggled, onPress, onPress, yes, no);
        //TODO: Decide if default implementation for right clicking should be do nothing, or act as left click
    }

    public TooltipToggleButton(IGuiWrapper gui, int x, int y, int width, int height, int textureWidth, int textureHeight, Identifier resource, BooleanSupplier isToggled,
          IClickable onLeftClick, IClickable onRightClick, @Nullable Tooltip yes, @Nullable Tooltip no) {
        super(gui, x, y, width, height, textureWidth, textureHeight, resource, onLeftClick, onRightClick);
        this.isToggled = isToggled;
        this.yes = yes;
        this.no = no;
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        setTooltip(isToggled.getAsBoolean() ? yes : no);
    }
}