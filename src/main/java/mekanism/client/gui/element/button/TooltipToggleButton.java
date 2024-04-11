package mekanism.client.gui.element.button;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.tooltip.TooltipUtils;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TooltipToggleButton extends MekanismImageButton {

    private final BooleanSupplier isToggled;
    private final Tooltip yes;
    private final Tooltip no;

    public TooltipToggleButton(IGuiWrapper gui, int x, int y, int size, ResourceLocation resource, BooleanSupplier isToggled, @NotNull IClickable onPress,
          Component yes, Component no) {
        super(gui, x, y, size, resource, onPress);
        this.isToggled = isToggled;
        this.yes = TooltipUtils.create(yes);
        this.no = TooltipUtils.create(no);
    }

    public TooltipToggleButton(IGuiWrapper gui, int x, int y, int size, ResourceLocation resource, BooleanSupplier isToggled, @NotNull IClickable onLeftClick, @NotNull IClickable onRightClick,
          Tooltip yes, Tooltip no) {
        this(gui, x, y, size, size, resource, isToggled, onLeftClick, onRightClick, yes, no);
    }

    public TooltipToggleButton(IGuiWrapper gui, int x, int y, int size, int textureSize, ResourceLocation resource, BooleanSupplier isToggled, @NotNull IClickable onLeftClick,
          @NotNull IClickable onRightClick, Tooltip yes, Tooltip no) {
        this(gui, x, y, size, size, textureSize, textureSize, resource, isToggled, onLeftClick, onRightClick, yes, no);
    }

    public TooltipToggleButton(IGuiWrapper gui, int x, int y, int size, int textureSize, ResourceLocation resource, BooleanSupplier isToggled, @NotNull IClickable onPress,
          Component yes, Component no) {
        this(gui, x, y, size, textureSize, resource, isToggled, onPress, TooltipUtils.create(yes), TooltipUtils.create(no));
    }

    public TooltipToggleButton(IGuiWrapper gui, int x, int y, int size, int textureSize, ResourceLocation resource, BooleanSupplier isToggled, @NotNull IClickable onPress,
          Tooltip yes, Tooltip no) {
        this(gui, x, y, size, size, textureSize, textureSize, resource, isToggled, onPress, onPress, yes, no);
        //TODO: Decide if default implementation for right clicking should be do nothing, or act as left click
    }

    public TooltipToggleButton(IGuiWrapper gui, int x, int y, int width, int height, int textureWidth, int textureHeight, ResourceLocation resource, BooleanSupplier isToggled,
          @NotNull IClickable onLeftClick, @NotNull IClickable onRightClick, Tooltip yes, Tooltip no) {
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