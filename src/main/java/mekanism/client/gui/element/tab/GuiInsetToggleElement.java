package mekanism.client.gui.element.tab;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import net.minecraft.resources.ResourceLocation;

public abstract class GuiInsetToggleElement<DATA_SOURCE> extends GuiInsetElement<DATA_SOURCE> {

    private final ResourceLocation flipped;
    private final BooleanSupplier isToggled;

    public GuiInsetToggleElement(IGuiWrapper gui, DATA_SOURCE dataSource, int x, int y, int height, int innerSize, boolean left, ResourceLocation overlay,
          ResourceLocation flipped, Predicate<DATA_SOURCE> isToggled) {
        this(gui, dataSource, x, y, height, innerSize, left, overlay, flipped, () -> isToggled.test(dataSource));
    }

    public GuiInsetToggleElement(IGuiWrapper gui, DATA_SOURCE dataSource, int x, int y, int height, int innerSize, boolean left, ResourceLocation overlay,
          ResourceLocation flipped, BooleanSupplier isToggled) {
        super(overlay, gui, dataSource, x, y, height, innerSize, left);
        this.isToggled = isToggled;
        this.flipped = flipped;
    }

    @Override
    protected ResourceLocation getOverlay() {
        return isToggled.getAsBoolean() ? flipped : super.getOverlay();
    }
}