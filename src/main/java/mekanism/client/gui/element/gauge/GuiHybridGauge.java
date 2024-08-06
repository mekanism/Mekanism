package mekanism.client.gui.element.gauge;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerIngredientHelper;
import mekanism.common.lib.transmitter.TransmissionType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class GuiHybridGauge extends GuiGauge<Void> implements IRecipeViewerIngredientHelper {

    private final Supplier<IChemicalTank> gasTankSupplier;

    private final GuiChemicalGauge gasGauge;
    private final GuiFluidGauge fluidGauge;

    private Component label;

    public GuiHybridGauge(Supplier<IChemicalTank> gasTankSupplier, Supplier<List<IChemicalTank>> gasTanksSupplier,
          Supplier<IExtendedFluidTank> fluidTankSupplier, Supplier<List<IExtendedFluidTank>> fluidTanksSupplier, GaugeType type,
          IGuiWrapper gui, int x, int y) {
        this(gasTankSupplier, gasTanksSupplier, fluidTankSupplier, fluidTanksSupplier, type, gui, x, y,
              type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
    }

    public GuiHybridGauge(Supplier<IChemicalTank> gasTankSupplier, Supplier<List<IChemicalTank>> gasTanksSupplier,
          Supplier<IExtendedFluidTank> fluidTankSupplier, Supplier<List<IExtendedFluidTank>> fluidTanksSupplier, GaugeType type,
          IGuiWrapper gui, int x, int y, int width, int height) {
        super(type, gui, x, y, width, height);
        this.gasTankSupplier = gasTankSupplier;
        gasGauge = addPositionOnlyChild(new GuiChemicalGauge(gasTankSupplier, gasTanksSupplier, type, gui, x, y, width, height));
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
    protected void applyRenderColor(GuiGraphics guiGraphics) {
        gasGauge.applyRenderColor(guiGraphics);
        fluidGauge.applyRenderColor(guiGraphics);
    }

    @Override
    public Optional<?> getIngredient(double mouseX, double mouseY) {
        Optional<?> gasIngredient = gasGauge.getIngredient(mouseX, mouseY);
        return gasIngredient.isPresent() ? gasIngredient : fluidGauge.getIngredient(mouseX, mouseY);
    }

    @Override
    public Rect2i getIngredientBounds(double mouseX, double mouseY) {
        Optional<?> gasIngredient = gasGauge.getIngredient(mouseX, mouseY);
        return gasIngredient.isPresent() ? gasGauge.getIngredientBounds(mouseX, mouseY) : fluidGauge.getIngredientBounds(mouseX, mouseY);
    }

    @Override
    public int getScaledLevel() {
        return Math.max(gasGauge.getScaledLevel(), fluidGauge.getScaledLevel());
    }

    @Nullable
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
        return gasTankSupplier.get() == null || !gasTankSupplier.get().isEmpty() ? TransmissionType.CHEMICAL : TransmissionType.FLUID;
    }
}