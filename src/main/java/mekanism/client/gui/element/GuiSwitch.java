package mekanism.client.gui.element;

import java.util.function.BooleanSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiSwitch extends GuiTexturedElement {

    private static final ResourceLocation SWITCH = MekanismUtils.getResource(ResourceType.GUI, "switch/switch.png");

    private final ResourceLocation icon;
    private final BooleanSupplier stateSupplier;
    private final ITextComponent tooltip;

    public GuiSwitch(IGuiWrapper gui, int x, int y, ResourceLocation icon, BooleanSupplier stateSupplier, ITextComponent tooltip) {
        super(SWITCH, gui, x, y, 17, 40);
        this.icon = icon;
        this.stateSupplier = stateSupplier;
        this.tooltip = tooltip;
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(tooltip, mouseX, mouseY);
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        blit(x, y, 0, stateSupplier.getAsBoolean() ? 0 : 10, 17, 10, 17, 20);
        blit(x, y + 11, 0, stateSupplier.getAsBoolean() ? 10 : 0, 17, 10, 17, 20);

        minecraft.textureManager.bindTexture(icon);
        blit(x + 6, y + 25, 0, 0, 5, 5, 5, 5);
    }

    @Override
    public void renderForeground(int mouseX, int mouseY, int xAxis, int yAxis) {
        super.renderForeground(mouseX, mouseY, xAxis, yAxis);

        renderScaledCenteredText(MekanismLang.ON.translate(), relativeX + 8, relativeY + 2, 0x101010, 0.7F);
        renderScaledCenteredText(MekanismLang.OFF.translate(), relativeX + 8, relativeY + 13, 0x101010, 0.7F);
    }
}
