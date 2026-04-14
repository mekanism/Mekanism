package mekanism.client.gui.element.tab;

import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.value.CachedBooleanValue;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class GuiToggleClientConfigTab extends GuiInsetToggleElement<IGuiWrapper> {

    private final Tooltip falseTooltip;
    private final Tooltip trueTooltip;
    private final CachedBooleanValue config;

    public GuiToggleClientConfigTab(IGuiWrapper gui, int y, boolean left, Identifier overlay, Identifier flipped, CachedBooleanValue config,
          Component trueTooltip, Component falseTooltip) {
        super(gui, gui, left ? -26 : gui.getXSize(), y, 26, 18, left, overlay, flipped, config);
        this.config = config;
        this.falseTooltip = TooltipUtils.create(falseTooltip);
        this.trueTooltip = TooltipUtils.create(trueTooltip);
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        setTooltip(config.get() ? trueTooltip : falseTooltip);
    }

    @Override
    protected void colorTab(GuiGraphicsExtractor guiGraphics) {
        MekanismRenderer.color(guiGraphics, SpecialColors.TAB_JEI_REJECTS_TARGET);
    }

    @Override
    public void onClick(@NotNull MouseButtonEvent event, boolean isDoubleClick) {
        config.set(!config.get());
        MekanismConfig.client.save();
    }
}