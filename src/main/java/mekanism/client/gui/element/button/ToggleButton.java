package mekanism.client.gui.element.button;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToggleButton extends MekanismImageButton {

    private static final ResourceLocation TOGGLE = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "toggle.png");
    private static final ResourceLocation TOGGLE_FLIPPED = MekanismUtils.getResource(ResourceType.GUI_BUTTON, "toggle_flipped.png");

    private final ResourceLocation flipped;
    private final BooleanSupplier toggled;

    public ToggleButton(IGuiWrapper gui, int x, int y, BooleanSupplier toggled, @NotNull IClickable onPress, @Nullable IHoverable onHover) {
        this(gui, x, y, 18, toggled, onPress, onHover);
    }

    public ToggleButton(IGuiWrapper gui, int x, int y, int size, BooleanSupplier toggled, @NotNull IClickable onPress, @Nullable IHoverable onHover) {
        this(gui, x, y, size, 18, TOGGLE, TOGGLE_FLIPPED, toggled, onPress, onHover);
    }

    public ToggleButton(IGuiWrapper gui, int x, int y, int size, int textureSize, ResourceLocation toggle, ResourceLocation flipped, BooleanSupplier toggled,
          @NotNull IClickable onPress, @Nullable IHoverable onHover) {
        this(gui, x, y, size, size, textureSize, textureSize, toggle, flipped, toggled, onPress, onHover);
    }

    public ToggleButton(IGuiWrapper gui, int x, int y, int width, int height, int textureWidth, int textureHeight, ResourceLocation toggle, ResourceLocation flipped,
          BooleanSupplier toggled, @NotNull IClickable onPress, @Nullable IHoverable onHover) {
        super(gui, x, y, width, height, textureWidth, textureHeight, toggle, onPress, onHover);
        this.toggled = toggled;
        this.flipped = flipped;
    }

    @Override
    protected ResourceLocation getResource() {
        return isToggled() ? flipped : super.getResource();
    }

    protected boolean isToggled() {
        return toggled.getAsBoolean();
    }
}