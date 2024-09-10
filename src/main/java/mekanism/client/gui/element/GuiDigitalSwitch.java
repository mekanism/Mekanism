package mekanism.client.gui.element;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismSounds;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GuiDigitalSwitch extends GuiTexturedElement {

    public static final ResourceLocation SWITCH = MekanismUtils.getResource(ResourceType.GUI, "switch/switch.png");
    public static final int BUTTON_SIZE_X = 15, BUTTON_SIZE_Y = 8;

    private final SwitchType type;
    private final ResourceLocation icon;
    private final BooleanSupplier stateSupplier;
    private final IClickable onToggle;

    public GuiDigitalSwitch(IGuiWrapper gui, int x, int y, ResourceLocation icon, BooleanSupplier stateSupplier, IClickable onToggle, SwitchType type) {
        super(SWITCH, gui, x, y, type.width, type.height);
        this.type = type;
        this.icon = icon;
        this.stateSupplier = stateSupplier;
        this.onToggle = onToggle;
        this.clickSound = () -> this.stateSupplier.getAsBoolean() ? MekanismSounds.BEEP_OFF.get() : MekanismSounds.BEEP_ON.get();
        this.clickVolume = 1.0F;
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        boolean state = stateSupplier.getAsBoolean();
        guiGraphics.blit(getResource(), relativeX + type.switchX, relativeY + type.switchY, 0, state ? 0 : BUTTON_SIZE_Y, BUTTON_SIZE_X, BUTTON_SIZE_Y, BUTTON_SIZE_X, BUTTON_SIZE_Y * 2);
        guiGraphics.blit(getResource(), relativeX + type.switchX, relativeY + type.switchY + BUTTON_SIZE_Y + 1, 0, state ? BUTTON_SIZE_Y : 0, BUTTON_SIZE_X, BUTTON_SIZE_Y, BUTTON_SIZE_X, BUTTON_SIZE_Y * 2);
        guiGraphics.blit(icon, relativeX + type.iconX, relativeY + type.iconY, 0, 0, 5, 5, 5, 5);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        drawScaledScrollingString(guiGraphics, MekanismLang.ON.translate(), type.switchX, type.switchY, TextAlignment.CENTER, 0x101010, BUTTON_SIZE_X, 1, false, 0.5F);
        drawScaledScrollingString(guiGraphics, MekanismLang.OFF.translate(), type.switchX, type.switchY + BUTTON_SIZE_Y + 1, TextAlignment.CENTER, 0x101010, BUTTON_SIZE_X, 1, false, 0.5F);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        onToggle.onClick(this, mouseX, mouseY);
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
