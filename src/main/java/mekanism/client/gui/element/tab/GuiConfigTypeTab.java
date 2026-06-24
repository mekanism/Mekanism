package mekanism.client.gui.element.tab;

import java.util.EnumMap;
import java.util.Map;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.SpecialColors;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.gui.element.window.GuiSideConfiguration;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.lib.transmitter.TransmissionType;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import org.jspecify.annotations.Nullable;

public class GuiConfigTypeTab extends GuiInsetElement<@Nullable Void> {

    private final Map<TransmissionType, Tooltip> typeTooltips = new EnumMap<>(TransmissionType.class);
    private final TransmissionType transmission;
    private final GuiSideConfiguration<?> config;

    public GuiConfigTypeTab(IGuiWrapper gui, TransmissionType type, int x, int y, GuiSideConfiguration<?> config, boolean left) {
        super(type.guiTexture(), gui, null, x, y, 26, 18, left);
        this.config = config;
        transmission = type;
    }

    public TransmissionType getTransmissionType() {
        return transmission;
    }

    @Override
    protected int getTabColor() {
        return (switch (transmission) {
            case ENERGY -> SpecialColors.TAB_ENERGY_CONFIG;
            case FLUID -> SpecialColors.TAB_FLUID_CONFIG;
            case CHEMICAL -> SpecialColors.TAB_CHEMICAL_CONFIG;
            case ITEM -> SpecialColors.TAB_ITEM_CONFIG;
            case HEAT -> SpecialColors.TAB_HEAT_CONFIG;
        }).argb();
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        setTooltip(typeTooltips.computeIfAbsent(transmission, trans -> TooltipUtils.create(TextComponentUtil.build(trans))));
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        config.setCurrentType(transmission);
        config.updateTabs();
    }
}