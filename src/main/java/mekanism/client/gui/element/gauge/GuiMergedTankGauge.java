package mekanism.client.gui.element.gauge;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerIngredientHelper;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.lib.transmitter.TransmissionType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class GuiMergedTankGauge<HANDLER extends IMekanismFluidHandler & IMekanismChemicalHandler> extends GuiGauge<Void> implements IRecipeViewerIngredientHelper {

    private final Supplier<MergedTank> mergedTankSupplier;
    private final Supplier<HANDLER> handlerSupplier;

    private final GuiFluidGauge fluidGauge;
    private final GuiChemicalGauge chemicalGauge;

    private Component label;

    public GuiMergedTankGauge(Supplier<MergedTank> mergedTankSupplier, Supplier<HANDLER> handlerSupplier, GaugeType type, IGuiWrapper gui, int x, int y) {
        this(mergedTankSupplier, handlerSupplier, type, gui, x, y, type.getGaugeOverlay().getWidth() + 2, type.getGaugeOverlay().getHeight() + 2);
    }

    public GuiMergedTankGauge(Supplier<MergedTank> mergedTankSupplier, Supplier<HANDLER> handlerSupplier, GaugeType type, IGuiWrapper gui, int x, int y, int width,
          int height) {
        super(type, gui, x, y, width, height);
        this.mergedTankSupplier = mergedTankSupplier;
        this.handlerSupplier = handlerSupplier;
        fluidGauge = addPositionOnlyChild(new GuiFluidGauge(() -> this.mergedTankSupplier.get().getFluidTank(), () -> this.handlerSupplier.get().getFluidTanks(null), type, gui, x, y, width, height));
        chemicalGauge = addPositionOnlyChild(new GuiChemicalGauge(() -> this.mergedTankSupplier.get().getChemicalTank(), () -> this.handlerSupplier.get().getChemicalTanks(null), type, gui, x, y, width, height));
    }

    public GuiMergedTankGauge<HANDLER> setLabel(Component label) {
        this.label = label;
        return this;
    }

    @Override
    public GaugeOverlay getGaugeOverlay() {
        return getCurrentGauge().getGaugeOverlay();
    }

    @Override
    protected GaugeInfo getGaugeColor() {
        return getCurrentGauge().getGaugeColor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        GuiTankGauge<?, ?> currentGauge = getCurrentGaugeNoFallback();
        if (currentGauge == null) {
            //If all the tanks are currently empty, pass the click event to all of them;
            // if multiple types are somehow stored in the dropper, insertion checks should prevent them from being inserted at the same time
            return fluidGauge.mouseClicked(mouseX, mouseY, button) | chemicalGauge.mouseClicked(mouseX, mouseY, button);
        }
        //Otherwise, just send the click event to the corresponding gauge
        return currentGauge.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void applyRenderColor(GuiGraphics guiGraphics) {
        GuiTankGauge<?, ?> currentGauge = getCurrentGaugeNoFallback();
        if (currentGauge != null) {
            currentGauge.applyRenderColor(guiGraphics);
        }
    }

    @Override
    public Optional<?> getIngredient(double mouseX, double mouseY) {
        GuiTankGauge<?, ?> currentGauge = getCurrentGaugeNoFallback();
        return currentGauge == null ? Optional.empty() : currentGauge.getIngredient(mouseX, mouseY);
    }

    @Override
    public Rect2i getIngredientBounds(double mouseX, double mouseY) {
        GuiTankGauge<?, ?> currentGauge = getCurrentGaugeNoFallback();
        return currentGauge == null ? new Rect2i(getX() + 1, getY() + 1, width - 2, height - 2) : currentGauge.getIngredientBounds(mouseX, mouseY);
    }

    @Override
    public int getScaledLevel() {
        GuiTankGauge<?, ?> currentGauge = getCurrentGaugeNoFallback();
        return currentGauge == null ? 0 : currentGauge.getScaledLevel();
    }

    @Nullable
    @Override
    public TextureAtlasSprite getIcon() {
        return getCurrentGauge().getIcon();
    }

    @Override
    public List<Component> getTooltipText() {
        return getCurrentGauge().getTooltipText();
    }

    @Override
    public Component getLabel() {
        return label;
    }

    @Override
    public TransmissionType getTransmission() {
        return getCurrentGauge().getTransmission();
    }

    private GuiTankGauge<?, ?> getCurrentGauge() {
        GuiTankGauge<?, ?> currentGauge = getCurrentGaugeNoFallback();
        //Fallback to the fluid gauge
        return currentGauge == null ? fluidGauge : currentGauge;
    }

    @Nullable
    private GuiTankGauge<?, ?> getCurrentGaugeNoFallback() {
        MergedTank mergedTank = mergedTankSupplier.get();
        return switch (mergedTank.getCurrentType()) {
            case FLUID -> fluidGauge;
            case CHEMICAL -> chemicalGauge;
            default -> null;
        };
    }
}