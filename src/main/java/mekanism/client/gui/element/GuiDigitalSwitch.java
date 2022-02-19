package mekanism.client.gui.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiDigitalSwitch extends GuiTexturedElement {

    public static final ResourceLocation SWITCH = MekanismUtils.getResource(ResourceType.GUI, "switch/switch.png");
    public static final int BUTTON_SIZE_X = 15, BUTTON_SIZE_Y = 8;

    private final SwitchType type;
    private final ResourceLocation icon;
    private final BooleanSupplier stateSupplier;
    private final ITextComponent tooltip;
    private final Runnable onToggle;

    public GuiDigitalSwitch(IGuiWrapper gui, int x, int y, ResourceLocation icon, BooleanSupplier stateSupplier, ITextComponent tooltip,
          Runnable onToggle, SwitchType type) {
        super(SWITCH, gui, x, y, type.width, type.height);
        this.type = type;
        this.icon = icon;
        this.stateSupplier = stateSupplier;
        this.tooltip = tooltip;
        this.onToggle = onToggle;
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        displayTooltip(matrix, tooltip, mouseX, mouseY);
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        minecraft.textureManager.bind(getResource());
        boolean state = stateSupplier.getAsBoolean();
        blit(matrix, x + type.switchX, y + type.switchY, 0, state ? 0 : BUTTON_SIZE_Y, BUTTON_SIZE_X, BUTTON_SIZE_Y, BUTTON_SIZE_X, BUTTON_SIZE_Y * 2);
        blit(matrix, x + type.switchX, y + type.switchY + BUTTON_SIZE_Y + 1, 0, state ? BUTTON_SIZE_Y : 0, BUTTON_SIZE_X, BUTTON_SIZE_Y, BUTTON_SIZE_X, BUTTON_SIZE_Y * 2);

        minecraft.textureManager.bind(icon);
        blit(matrix, x + type.iconX, y + type.iconY, 0, 0, 5, 5, 5, 5);
    }

    @Override
    public void renderForeground(MatrixStack matrix, int mouseX, int mouseY) {
        super.renderForeground(matrix, mouseX, mouseY);
        drawScaledCenteredText(matrix, MekanismLang.ON.translate(), relativeX + type.switchX + 8, relativeY + type.switchY, 0x101010, 0.5F);
        drawScaledCenteredText(matrix, MekanismLang.OFF.translate(), relativeX + type.switchX + 8, relativeY + type.switchY + 9, 0x101010, 0.5F);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(MekanismSounds.BEEP.get(), 1.0F));
        onToggle.run();
    }

    public enum SwitchType {
        LOWER_ICON(BUTTON_SIZE_X, BUTTON_SIZE_Y * 2 + 15, 0, 0, 5, 21),
        LEFT_ICON(BUTTON_SIZE_X + 15, BUTTON_SIZE_Y * 2, 15, 0, 5, 6);

        private final int iconX, iconY;
        private final int width, height;
        private final int switchX, switchY;

        SwitchType(int width, int height, int switchX, int switchY, int iconX, int iconY) {
            this.width = width;
            this.height = height;
            this.iconX = iconX;
            this.iconY = iconY;
            this.switchX = switchX;
            this.switchY = switchY;
        }
    }
}
