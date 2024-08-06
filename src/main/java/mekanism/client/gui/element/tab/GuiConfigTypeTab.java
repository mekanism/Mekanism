package mekanism.client.gui.element.tab;

import java.util.EnumMap;
import java.util.Map;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.gui.element.window.GuiSideConfiguration;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.resources.ResourceLocation;

public class GuiConfigTypeTab extends GuiInsetElement<Void> {

    private final Map<TransmissionType, Tooltip> typeTooltips = new EnumMap<>(TransmissionType.class);
    private final TransmissionType transmission;
    private final GuiSideConfiguration<?> config;

    public GuiConfigTypeTab(IGuiWrapper gui, TransmissionType type, int x, int y, GuiSideConfiguration<?> config, boolean left) {
        super(getResource(type), gui, null, x, y, 26, 18, left);
        this.config = config;
        transmission = type;
    }

    private static ResourceLocation getResource(TransmissionType t) {
        return MekanismUtils.getResource(ResourceType.GUI, t.getTransmission() + ".png");
    }

    public TransmissionType getTransmissionType() {
        return transmission;
    }

    @Override
    protected void colorTab(GuiGraphics guiGraphics) {
        MekanismRenderer.color(guiGraphics, switch (transmission) {
            case ENERGY -> SpecialColors.TAB_ENERGY_CONFIG;
            case FLUID -> SpecialColors.TAB_FLUID_CONFIG;
            case CHEMICAL -> SpecialColors.TAB_CHEMICAL_CONFIG;
            case ITEM -> SpecialColors.TAB_ITEM_CONFIG;
            case HEAT -> SpecialColors.TAB_HEAT_CONFIG;
        });
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        setTooltip(typeTooltips.computeIfAbsent(transmission, trans -> TooltipUtils.create(TextComponentUtil.build(trans))));
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        config.setCurrentType(transmission);
        config.updateTabs();
    }
}