package mekanism.client.gui.element;

import java.util.function.Supplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.tile.TileEntityGasTank.GasMode;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

//TODO: Should this extend MekanismImageButton
public class GuiGasMode extends GuiTexturedElement {

    private static final ResourceLocation IDLE = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "gas_mode_idle.png");
    private static final ResourceLocation EXCESS = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "gas_mode_excess.png");
    private static final ResourceLocation DUMP = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "gas_mode_dump.png");

    private final boolean left;
    private final Supplier<GasMode> gasModeSupplier;
    private final Runnable onPress;

    public GuiGasMode(IGuiWrapper gui, int x, int y, boolean left, Supplier<GasMode> gasModeSupplier, Runnable onPress) {
        super(IDLE, gui, x, y, 10, 10);
        this.left = left;
        this.gasModeSupplier = gasModeSupplier;
        this.onPress = onPress;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        onPress.run();
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
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        //Draw the button background
        super.renderButton(mouseX, mouseY, partialTicks);
        //Draw overlay
        minecraft.textureManager.bindTexture(getResource());
        guiObj.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, 10, 10);
        //Draw the text next to the button
        ITextComponent component = gasModeSupplier.get().getTextComponent();
        if (left) {
            renderScaledText(component, x - 3 - (int) (getStringWidth(component) * getNeededScale(component, 66)), y + 1, 0x404040, 66);
        } else {
            renderScaledText(component, x + width + 5, y + 1, 0x404040, 66);
        }
    }
}