package mekanism.client.gui.element.gauge;

import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.jei.IJEIIngredientHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;

public class GuiHybridGauge extends GuiGauge<Void> implements IJEIIngredientHelper {

    private Supplier<IGasTank> gasTankSupplier;
    private Supplier<IExtendedFluidTank> fluidTankSupplier;

    private GuiGasGauge gasGauge;
    private GuiFluidGauge fluidGauge;

    private ITextComponent label;

    public GuiHybridGauge(Supplier<IGasTank> gasTankSupplier, Supplier<List<IGasTank>> gasTanksSupplier,
          Supplier<IExtendedFluidTank> fluidTankSupplier, Supplier<List<IExtendedFluidTank>> fluidTanksSupplier, GaugeType type,
        IGuiWrapper gui, int x, int y) {
        super(type, gui, x, y);
        gasGauge = new GuiGasGauge(gasTankSupplier, gasTanksSupplier, type, gui, x, y);
        fluidGauge = new GuiFluidGauge(fluidTankSupplier, fluidTanksSupplier, type, gui, x, y);
    }

    public GuiHybridGauge setLabel(ITextComponent label) {
        this.label = label;
        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // pass the click event to both gauges; if both a fluid and gas are stored in the dropper, insertion checks should prevent both from being
        // inserted at the same time
        gasGauge.mouseClicked(mouseX, mouseY, button);
        fluidGauge.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void applyRenderColor() {
        gasGauge.applyRenderColor();
        fluidGauge.applyRenderColor();
    }

    @Nullable
    @Override
    public Object getIngredient() {
        return gasGauge.getIngredient() != null ? gasGauge.getIngredient() : fluidGauge.getIngredient();
    }

    @Override
    public int getScaledLevel() {
        return Math.max(gasGauge.getScaledLevel(), fluidGauge.getScaledLevel());
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return gasTankSupplier.get() != null && !gasTankSupplier.get().isEmpty() ? gasGauge.getIcon() : fluidGauge.getIcon();
    }

    @Override
    public List<ITextComponent> getTooltipText() {
        return gasTankSupplier.get() != null && !gasTankSupplier.get().isEmpty() ? gasGauge.getTooltipText() : fluidGauge.getTooltipText();
    }

    @Override
    public ITextComponent getLabel() {
        return label;
    }

    @Override
    public TransmissionType getTransmission() {
        return gasTankSupplier.get() != null && gasTankSupplier.get().isEmpty() ? TransmissionType.FLUID : TransmissionType.GAS;
    }
}