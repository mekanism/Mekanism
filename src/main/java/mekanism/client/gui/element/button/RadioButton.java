package mekanism.client.gui.element.button;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RadioButton extends MekanismButton {

    public static final ResourceLocation RADIO = MekanismUtils.getResource(ResourceType.GUI, "radio_button.png");
    public static final int RADIO_SIZE = 8;

    private final BooleanSupplier toggled;

    public RadioButton(IGuiWrapper gui, int x, int y, BooleanSupplier toggled, @NotNull IClickable onPress, @Nullable IHoverable onHover) {
        super(gui, x, y, RADIO_SIZE, RADIO_SIZE, Component.empty(), onPress, onHover);
        this.toggled = toggled;
        this.clickSound = MekanismSounds.BEEP;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        if (toggled.getAsBoolean()) {
            guiGraphics.blit(RADIO, getButtonX(), getButtonY(), 0, RADIO_SIZE, getButtonWidth(), getButtonHeight(), 2 * RADIO_SIZE, 2 * RADIO_SIZE);
        } else {
            int uOffset = checkWindows(mouseX, mouseY, isHoveredOrFocused()) ? RADIO_SIZE : 0;
            guiGraphics.blit(RADIO, getButtonX(), getButtonY(), uOffset, 0, getButtonWidth(), getButtonHeight(), 2 * RADIO_SIZE, 2 * RADIO_SIZE);
        }
    }
}