package mekanism.client.gui.element;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.text.EnumColor;
import mekanism.client.gui.GuiUtils;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.window.GuiColorWindow;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.MekanismLang;
import mekanism.common.lib.Color;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import org.jetbrains.annotations.Nullable;

public class GuiColorPickerSlot extends GuiElement {

    private final Supplier<Color> supplier;
    private final Consumer<Color> consumer;
    private final boolean handlesAlpha;

    @Nullable
    private Tooltip lastTooltip = null;
    @Nullable
    private Color lastColor = null;


    public GuiColorPickerSlot(IGuiWrapper gui, int x, int y, boolean handlesAlpha, Supplier<Color> supplier, Consumer<Color> consumer) {
        super(gui, x, y, 18, 18);
        this.handlesAlpha = handlesAlpha;
        this.supplier = supplier;
        this.consumer = consumer;
        addChild(new GuiElementHolder(gui, relativeX, relativeY, 18, 18));
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        Color color = supplier.get();
        if (!color.equals(lastColor)) {
            lastColor = color;
            lastTooltip = TooltipUtils.create(MekanismLang.GENERIC_HEX.translateColored(EnumColor.GRAY, TextUtils.hex(false, 3, color.rgb())));
        }
        setTooltip(lastTooltip);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderForeground(guiGraphics, mouseX, mouseY);
        GuiUtils.fill(guiGraphics, relativeX + 1, relativeY + 1, width - 2, height - 2, supplier.get().argb());
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        gui().addWindow(new GuiColorWindow(gui(), (getGuiWidth() - 160) / 2, (getGuiHeight() - 120) / 2, handlesAlpha, supplier.get(), consumer));
    }
}
