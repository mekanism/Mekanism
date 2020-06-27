package mekanism.client.gui.element;

import static mekanism.client.gui.element.GuiDigitalSwitch.BUTTON_SIZE_X;
import static mekanism.client.gui.element.GuiDigitalSwitch.BUTTON_SIZE_Y;
import static mekanism.client.gui.element.GuiDigitalSwitch.SWITCH;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Collections;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
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
        super(gui, x, y, width, BUTTON_SIZE_Y * 2 + 5, () -> Collections.singletonList(buttonName));
        this.stateSupplier = stateSupplier;
        this.onToggle = onToggle;
        field_230693_o_ = true;
    }

    @Override
    public void func_230431_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.func_230431_b_(matrix, mouseX, mouseY, partialTicks);
        minecraft.textureManager.bindTexture(SWITCH);
        func_238463_a_(matrix, field_230690_l_ + field_230688_j_ - 2 - BUTTON_SIZE_X, field_230691_m_ + 2, 0, stateSupplier.getAsBoolean() ? 0 : BUTTON_SIZE_Y, BUTTON_SIZE_X, BUTTON_SIZE_Y, BUTTON_SIZE_X, BUTTON_SIZE_Y * 2);
        func_238463_a_(matrix, field_230690_l_ + field_230688_j_ - 2 - BUTTON_SIZE_X, field_230691_m_ + 2 + BUTTON_SIZE_Y + 1, 0, stateSupplier.getAsBoolean() ? BUTTON_SIZE_Y : 0, BUTTON_SIZE_X, BUTTON_SIZE_Y, BUTTON_SIZE_X, BUTTON_SIZE_Y * 2);
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        drawScaledCenteredText(matrix, MekanismLang.ON.translate(), relativeX + field_230688_j_ - 9, relativeY + 3, 0x101010, 0.5F);
        drawScaledCenteredText(matrix, MekanismLang.OFF.translate(), relativeX + field_230688_j_ - 9, relativeY + 12, 0x101010, 0.5F);
    }

    @Override
    public void func_230982_a_(double mouseX, double mouseY) {
        Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(MekanismSounds.BEEP.get(), 1.0F));
        onToggle.run();
    }
}
