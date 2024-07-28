package mekanism.client.gui.element;

import java.util.Collections;
import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GuiScreenSwitch extends GuiInnerScreen {

    private final BooleanSupplier stateSupplier;
    private final IClickable onToggle;

    public GuiScreenSwitch(IGuiWrapper gui, int x, int y, int width, Component buttonName, BooleanSupplier stateSupplier, IClickable onToggle) {
        super(gui, x, y, width, GuiDigitalSwitch.BUTTON_SIZE_Y * 2 + 5, () -> Collections.singletonList(buttonName));
        this.stateSupplier = stateSupplier;
        this.onToggle = onToggle;
        this.active = true;
        this.clickSound = () -> this.stateSupplier.getAsBoolean() ? MekanismSounds.BEEP_OFF.get() : MekanismSounds.BEEP_ON.get();
        this.clickVolume = 1.0F;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        int buttonSizeX = GuiDigitalSwitch.BUTTON_SIZE_X;
        int buttonSizeY = GuiDigitalSwitch.BUTTON_SIZE_Y;
        guiGraphics.blit(GuiDigitalSwitch.SWITCH, relativeX + width - 2 - buttonSizeX, relativeY + 2, 0, stateSupplier.getAsBoolean() ? 0 : buttonSizeY, buttonSizeX, buttonSizeY, buttonSizeX, buttonSizeY * 2);
        guiGraphics.blit(GuiDigitalSwitch.SWITCH, relativeX + width - 2 - buttonSizeX, relativeY + 2 + buttonSizeY + 1, 0, stateSupplier.getAsBoolean() ? buttonSizeY : 0, buttonSizeX, buttonSizeY, buttonSizeX, buttonSizeY * 2);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawScaledCenteredText(guiGraphics, MekanismLang.ON.translate(), relativeX + width - 9, relativeY + 2, 0x101010, 0.5F);
        drawScaledCenteredText(guiGraphics, MekanismLang.OFF.translate(), relativeX + width - 9, relativeY + 11, 0x101010, 0.5F);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        onToggle.onClick(this, mouseX, mouseY);
    }

    @Override
    protected int getMaxTextWidth() {
        return super.getMaxTextWidth() - 2 - GuiDigitalSwitch.BUTTON_SIZE_X;
    }
}
