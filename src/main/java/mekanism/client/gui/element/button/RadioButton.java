package mekanism.client.gui.element.button;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class RadioButton extends MekanismButton {

    public static final Identifier BASE = Mekanism.rl("button/radio/base");
    public static final Identifier HOVERED = Mekanism.rl("button/radio/hovered");
    public static final Identifier NO_SELECTED = Mekanism.rl("button/radio/no_selected");
    public static final Identifier SELECTED = Mekanism.rl("button/radio/selected");
    public static final int RADIO_SIZE = 8;

    @Nullable
    private final Tooltip toggledComponent;
    @Nullable
    private final Tooltip altComponent;
    private final BooleanSupplier toggled;

    public RadioButton(IGuiWrapper gui, int x, int y, BooleanSupplier toggled, IClickable onPress, Component toggledComponent, Component altComponent) {
        super(gui, x, y, RADIO_SIZE, RADIO_SIZE, CommonComponents.EMPTY, onPress);
        this.toggled = toggled;
        this.toggledComponent = TooltipUtils.create(toggledComponent);
        this.altComponent = TooltipUtils.create(altComponent);
        this.clickSound = () -> this.toggled.getAsBoolean() ? MekanismSounds.BEEP_OFF.get() : MekanismSounds.BEEP_ON.get();
        this.clickVolume = 1.0F;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        if (toggled.getAsBoolean()) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SELECTED, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight());
        } else {
            Identifier sprite = checkWindows(mouseX, mouseY, isHoveredOrFocused()) ? HOVERED : BASE;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight());
        }
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        setTooltip(toggled.getAsBoolean() ? toggledComponent : altComponent);
    }
}