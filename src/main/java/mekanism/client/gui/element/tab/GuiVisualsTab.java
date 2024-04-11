package mekanism.client.gui.element.tab;

import mekanism.api.text.EnumColor;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.tile.interfaces.IHasVisualization;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class GuiVisualsTab extends GuiInsetElement<IHasVisualization> {

    private static final Component ON = MekanismLang.VISUALS.translate(OnOff.ON);
    private static final Component OFF = MekanismLang.VISUALS.translate(OnOff.OFF);
    private static final Component TOO_BIG = MekanismLang.VISUALS_TOO_BIG.translateColored(EnumColor.RED);
    private static final Tooltip VISUALS_ON = TooltipUtils.create(ON);
    private static final Tooltip VISUALS_OFF = TooltipUtils.create(OFF);
    private static final Tooltip VISUALS_ON_TOO_BIG = TooltipUtils.create(ON, TOO_BIG);
    private static final Tooltip VISUALS_OFF_TOO_BIG = TooltipUtils.create(OFF, TOO_BIG);

    public GuiVisualsTab(IGuiWrapper gui, IHasVisualization hasVisualization) {
        super(MekanismUtils.getResource(ResourceType.GUI, "visuals.png"), gui, hasVisualization, -26, 6, 26, 18, true);
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        if (dataSource.canDisplayVisuals()) {
            setTooltip(dataSource.isClientRendering() ? VISUALS_ON : VISUALS_OFF);
        } else {
            setTooltip(dataSource.isClientRendering() ? VISUALS_ON_TOO_BIG : VISUALS_OFF_TOO_BIG);
        }
    }

    @Override
    protected void colorTab(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, SpecialColors.TAB_VISUALS);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        dataSource.toggleClientRendering();
    }
}