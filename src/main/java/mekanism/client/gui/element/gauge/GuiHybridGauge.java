package mekanism.client.gui.element.gauge;

import java.util.List;
import java.util.function.Supplier;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.jei.interfaces.IJEIIngredientHelper;
import mekanism.common.lib.transmitter.TransmissionType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class GuiHybridGauge extends GuiGauge<Void> implements IJEIIngredientHelper {

    private final Supplier<IGasTank> gasTankSupplier;

    private final GuiGasGauge gasGauge;
    private final GuiFluidGauge fluidGauge;

    private Component label;

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
        gasGauge = addPositionOnlyChild(new GuiGasGauge(gasTankSupplier, gasTanksSupplier, type, gui, x, y, width, height));
        fluidGauge = addPositionOnlyChild(new GuiFluidGauge(fluidTankSupplier, fluidTanksSupplier, type, gui, x, y, width, height));
    }

    public GuiHybridGauge setLabel(Component label) {
        this.label = label;
        return this;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // pass the click event to both gauges; if both a fluid and gas are stored in the dropper, insertion checks should prevent both from being
        // inserted at the same time
        return gasGauge.mouseClicked(mouseX, mouseY, button) | fluidGauge.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void applyRenderColor() {
        gasGauge.applyRenderColor();
        fluidGauge.applyRenderColor();
    }

    @Nullable
    @Override
    public Object getIngredient(double mouseX, double mouseY) {
        Object gasIngredient = gasGauge.getIngredient(mouseX, mouseY);
        return gasIngredient == null ? fluidGauge.getIngredient(mouseX, mouseY) : gasIngredient;
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
    public List<Component> getTooltipText() {
        return gasTankSupplier.get() == null || gasTankSupplier.get().isEmpty() ? fluidGauge.getTooltipText() : gasGauge.getTooltipText();
    }

    @Override
    public Component getLabel() {
        return label;
    }

    @Override
    public TransmissionType getTransmission() {
        return gasTankSupplier.get() == null || !gasTankSupplier.get().isEmpty() ? TransmissionType.GAS : TransmissionType.FLUID;
    }
}