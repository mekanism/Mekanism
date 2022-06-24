package mekanism.client.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collections;
import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class GuiScreenSwitch extends GuiInnerScreen {

    private final BooleanSupplier stateSupplier;
    private final Runnable onToggle;

    public GuiScreenSwitch(IGuiWrapper gui, int x, int y, int width, Component buttonName, BooleanSupplier stateSupplier, Runnable onToggle) {
        super(gui, x, y, width, GuiDigitalSwitch.BUTTON_SIZE_Y * 2 + 5, () -> Collections.singletonList(buttonName));
        this.stateSupplier = stateSupplier;
        this.onToggle = onToggle;
        active = true;
    }

    @Override
    public void drawBackground(@NotNull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        RenderSystem.setShaderTexture(0, GuiDigitalSwitch.SWITCH);
        int buttonSizeX = GuiDigitalSwitch.BUTTON_SIZE_X;
        int buttonSizeY = GuiDigitalSwitch.BUTTON_SIZE_Y;
        blit(matrix, x + width - 2 - buttonSizeX, y + 2, 0, stateSupplier.getAsBoolean() ? 0 : buttonSizeY, buttonSizeX, buttonSizeY, buttonSizeX, buttonSizeY * 2);
        blit(matrix, x + width - 2 - buttonSizeX, y + 2 + buttonSizeY + 1, 0, stateSupplier.getAsBoolean() ? buttonSizeY : 0, buttonSizeX, buttonSizeY, buttonSizeX, buttonSizeY * 2);
    }

    @Override
    public void renderForeground(PoseStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        drawScaledCenteredText(matrix, MekanismLang.ON.translate(), relativeX + width - 9, relativeY + 2, 0x101010, 0.5F);
        drawScaledCenteredText(matrix, MekanismLang.OFF.translate(), relativeX + width - 9, relativeY + 11, 0x101010, 0.5F);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(MekanismSounds.BEEP.get(), 1.0F));
        onToggle.run();
    }
}
