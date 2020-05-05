package mekanism.client.gui.element;

import static mekanism.client.gui.element.GuiDigitalSwitch.BUTTON_SIZE_X;
import static mekanism.client.gui.element.GuiDigitalSwitch.BUTTON_SIZE_Y;
import static mekanism.client.gui.element.GuiDigitalSwitch.SWITCH;
import java.util.Arrays;
import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.text.ITextComponent;

public class GuiScreenSwitch extends GuiInnerScreen {

    private final BooleanSupplier stateSupplier;
    private final Runnable onToggle;

    public GuiScreenSwitch(IGuiWrapper gui, int x, int y, int width, ITextComponent buttonName, BooleanSupplier stateSupplier, Runnable onToggle) {
        super(gui, x, y, width, BUTTON_SIZE_Y * 2 + 5, () -> Arrays.asList(buttonName));
        this.stateSupplier = stateSupplier;
        this.onToggle = onToggle;
        active = true;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        super.renderButton(mouseX, mouseY, partialTicks);
        minecraft.textureManager.bindTexture(SWITCH);
        blit(x + width - 2 - BUTTON_SIZE_X, y + 2, 0, stateSupplier.getAsBoolean() ? 0 : BUTTON_SIZE_Y, BUTTON_SIZE_X, BUTTON_SIZE_Y, BUTTON_SIZE_X, BUTTON_SIZE_Y * 2);
        blit(x + width - 2 - BUTTON_SIZE_X, y + 2 + BUTTON_SIZE_Y + 1, 0, stateSupplier.getAsBoolean() ? BUTTON_SIZE_Y : 0, BUTTON_SIZE_X, BUTTON_SIZE_Y, BUTTON_SIZE_X, BUTTON_SIZE_Y * 2);
    }

    @Override
    public void renderForeground(int mouseX, int mouseY) {
        super.renderForeground(mouseX, mouseY);

        drawScaledCenteredText(MekanismLang.ON.translate(), relativeX + width - 9, relativeY + 3, 0x101010, 0.5F);
        drawScaledCenteredText(MekanismLang.OFF.translate(), relativeX + width - 9, relativeY + 12, 0x101010, 0.5F);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(MekanismSounds.BEEP.get(), 1.0F));
        onToggle.run();
    }
}
