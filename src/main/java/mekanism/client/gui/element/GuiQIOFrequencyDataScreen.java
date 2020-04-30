package mekanism.client.gui.element;

import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.content.qio.QIOFrequency;

public class GuiQIOFrequencyDataScreen extends GuiInnerScreen {

    private Supplier<QIOFrequency> frequencySupplier;

    public GuiQIOFrequencyDataScreen(IGuiWrapper gui, int x, int y, int width, int height, Supplier<QIOFrequency> frequencySupplier) {
        super(gui, x, y, width, height);
        this.frequencySupplier = frequencySupplier;
    }

    @Override
    public void renderForeground(int mouseX, int mouseY, int xAxis, int yAxis) {
        super.renderForeground(mouseX, mouseY, xAxis, yAxis);
    }
}
