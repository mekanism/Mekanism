package mekanism.client.gui.element.button;

import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiGasMode extends MekanismImageButton {

    private static final ResourceLocation IDLE = MekanismUtils.getResource(ResourceType.GUI, "gas_mode_idle.png");
    private static final ResourceLocation EXCESS = MekanismUtils.getResource(ResourceType.GUI, "gas_mode_excess.png");
    private static final ResourceLocation DUMP = MekanismUtils.getResource(ResourceType.GUI, "gas_mode_dump.png");

    private final boolean left;
    private final Supplier<GasMode> gasModeSupplier;

    public GuiGasMode(IGuiWrapper gui, int x, int y, boolean left, Supplier<GasMode> gasModeSupplier, Runnable onPress) {
        super(gui, x, y, 10, IDLE, onPress);
        this.left = left;
        this.gasModeSupplier = gasModeSupplier;
    }

    @Override
    protected ResourceLocation getResource() {
        GasMode mode = gasModeSupplier.get();
        switch (mode) {
            case DUMPING_EXCESS:
                return EXCESS;
            case DUMPING:
                return DUMP;
        }
        return super.getResource();
    }

    @Override
    public void renderForeground(int mouseX, int mouseY, int xAxis, int yAxis) {
        //Draw the text next to the button
        ITextComponent component = gasModeSupplier.get().getTextComponent();
        int xPos = x - guiObj.getLeft();
        int yPos = y - guiObj.getTop();
        if (left) {
            renderScaledText(component, xPos - 3 - (int) (getStringWidth(component) * getNeededScale(component, 66)), yPos + 1, 0x404040, 66);
        } else {
            renderScaledText(component, xPos + width + 5, yPos + 1, 0x404040, 66);
        }
        super.renderForeground(mouseX, mouseY, xAxis, yAxis);
    }
}