package mekanism.client.gui.element.bar;

import com.mojang.blaze3d.matrix.MatrixStack;
import mekanism.api.energy.IEnergyContainer;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiHorizontalPowerBar extends GuiBar<IBarInfoHandler> {

    private static final ResourceLocation ENERGY_BAR = MekanismUtils.getResource(ResourceType.GUI_BAR, "horizontal_power.png");
    private static final int texWidth = 52;
    private static final int texHeight = 4;

    private final double widthScale;

    public GuiHorizontalPowerBar(IGuiWrapper gui, IEnergyContainer container, int x, int y) {
        this(gui, container, x, y, texWidth);
    }

    public GuiHorizontalPowerBar(IGuiWrapper gui, IEnergyContainer container, int x, int y, int desiredWidth) {
        this(gui, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return EnergyDisplay.of(container.getEnergy(), container.getMaxEnergy()).getTextComponent();
            }

            @Override
            public double getLevel() {
                return container.getEnergy().divideToLevel(container.getMaxEnergy());
            }
        }, x, y, desiredWidth);
    }

    public GuiHorizontalPowerBar(IGuiWrapper gui, IBarInfoHandler handler, int x, int y, int desiredWidth) {
        super(ENERGY_BAR, gui, handler, x, y, desiredWidth, texHeight);
        widthScale = desiredWidth / (double) texWidth;
    }

    @Override
    protected void renderBarOverlay(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        int displayInt = (int) (getHandler().getLevel() * texWidth);
        blit(matrix, x + 1, y + 1, calculateScaled(widthScale, displayInt), texHeight, 0, 0, displayInt, texHeight, texWidth, texHeight);
    }
}