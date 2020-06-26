package mekanism.client.gui.element.gauge;

import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.jei.interfaces.IJEIIngredientHelper;
import mekanism.common.lib.transmitter.TransmissionType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;

public class GuiHybridGauge extends GuiGauge<Void> implements IJEIIngredientHelper {

    private final Supplier<IGasTank> gasTankSupplier;
    private final Supplier<IExtendedFluidTank> fluidTankSupplier;

    private final GuiGasGauge gasGauge;
    private final GuiFluidGauge fluidGauge;

    private ITextComponent label;

    public GuiHybridGauge(Supplier<IGasTank> gasTankSupplier, Supplier<List<IGasTank>> gasTanksSupplier,
          Supplier<IExtendedFluidTank> fluidTankSupplier, Supplier<List<IExtendedFluidTank>> fluidTanksSupplier, GaugeType type,
          IGuiWrapper gui, int x, int y) {
        this(gasTankSupplier, gasTanksSupplier, fluidTankSupplier, fluidTanksSupplier, type, gui, x, y,
              type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
    }

    public GuiHybridGauge(Supplier<IGasTank> gasTankSupplier, Supplier<List<IGasTank>> gasTanksSupplier,
          Supplier<IExtendedFluidTank> fluidTankSupplier, Supplier<List<IExtendedFluidTank>> fluidTanksSupplier, GaugeType type,
          IGuiWrapper gui, int x, int y, int width, int height) {
        super(type, gui, x, y, width, height);
        this.gasTankSupplier = gasTankSupplier;
        this.fluidTankSupplier = fluidTankSupplier;
        gasGauge = new GuiGasGauge(gasTankSupplier, gasTanksSupplier, type, gui, x, y, width, height);
        fluidGauge = new GuiFluidGauge(fluidTankSupplier, fluidTanksSupplier, type, gui, x, y, width, height);
    }

    public GuiHybridGauge setLabel(ITextComponent label) {
        this.label = label;
        return this;
    }

    @Override
    public boolean func_231044_a_(double mouseX, double mouseY, int button) {
        // pass the click event to both gauges; if both a fluid and gas are stored in the dropper, insertion checks should prevent both from being
        // inserted at the same time
        gasGauge.func_231044_a_(mouseX, mouseY, button);
        fluidGauge.func_231044_a_(mouseX, mouseY, button);
        return super.func_231044_a_(mouseX, mouseY, button);
    }

    @Override
    protected void applyRenderColor() {
        gasGauge.applyRenderColor();
        fluidGauge.applyRenderColor();
    }

    @Nullable
    @Override
    public Object getIngredient() {
        return gasGauge.getIngredient() == null ? fluidGauge.getIngredient() : gasGauge.getIngredient();
    }

    @Override
    public int getScaledLevel() {
        return Math.max(gasGauge.getScaledLevel(), fluidGauge.getScaledLevel());
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return gasTankSupplier.get() == null || gasTankSupplier.get().isEmpty() ? fluidGauge.getIcon() : gasGauge.getIcon();
    }

    @Override
    public List<ITextComponent> getTooltipText() {
        return gasTankSupplier.get() == null || gasTankSupplier.get().isEmpty() ? fluidGauge.getTooltipText() : gasGauge.getTooltipText();
    }

    @Override
    public ITextComponent getLabel() {
        return label;
    }

    @Override
    public TransmissionType getTransmission() {
        return gasTankSupplier.get() == null || !gasTankSupplier.get().isEmpty() ? TransmissionType.GAS : TransmissionType.FLUID;
    }
}