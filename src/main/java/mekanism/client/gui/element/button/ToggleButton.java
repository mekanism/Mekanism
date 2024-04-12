package mekanism.client.gui.element.button;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToggleButton extends MekanismImageButton {

    private static final ResourceLocation TOGGLE = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "toggle.png");
    private static final ResourceLocation TOGGLE_FLIPPED = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "toggle_flipped.png");

    private final ResourceLocation flipped;
    private final BooleanSupplier toggled;
    @Nullable
    private final Tooltip yes;
    @Nullable
    private final Tooltip no;

    public ToggleButton(IGuiWrapper gui, int x, int y, BooleanSupplier toggled, @NotNull IClickable onPress) {
        this(gui, x, y, 18, toggled, onPress);
    }

    public ToggleButton(IGuiWrapper gui, int x, int y, int size, BooleanSupplier toggled, @NotNull IClickable onPress) {
        this(gui, x, y, size, 18, TOGGLE, TOGGLE_FLIPPED, toggled, onPress, null, null);
    }

    public ToggleButton(IGuiWrapper gui, int x, int y, int size, int textureSize, ResourceLocation toggle, ResourceLocation flipped, BooleanSupplier toggled,
          @NotNull IClickable onPress, @Nullable Component yes, @Nullable Component no) {
        this(gui, x, y, size, size, textureSize, textureSize, toggle, flipped, toggled, onPress, yes, no);
    }

    public ToggleButton(IGuiWrapper gui, int x, int y, int width, int height, int textureWidth, int textureHeight, ResourceLocation toggle, ResourceLocation flipped,
          BooleanSupplier toggled, @NotNull IClickable onPress, @Nullable Component yes, @Nullable Component no) {
        super(gui, x, y, width, height, textureWidth, textureHeight, toggle, onPress);
        this.toggled = toggled;
        this.flipped = flipped;
        this.yes = TooltipUtils.create(yes);
        this.no = TooltipUtils.create(no);
    }

    @Override
    protected ResourceLocation getResource() {
        return toggled.getAsBoolean() ? flipped : super.getResource();
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        setTooltip(toggled.getAsBoolean() ? yes : no);
    }
}