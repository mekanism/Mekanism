package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import mekanism.api.IIncrementalEnum;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class GuiHeatTab extends GuiTexturedElement {

    private static final Map<TemperatureUnit, Identifier> ICONS = new EnumMap<>(TemperatureUnit.class);
    private final IInfoHandler infoHandler;

    private List<Component> lastInfo = Collections.emptyList();
    @Nullable
    private Tooltip lastTooltip;

    public GuiHeatTab(IGuiWrapper gui, IInfoHandler handler) {
        super(MekanismUtils.getResource(ResourceType.GUI_TAB, "heat_info.png"), gui, -26, 109, 26, 26);
        infoHandler = handler;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getResource(), relativeX, relativeY, 0, 0, width, height, width, height);
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        List<Component> info = new ArrayList<>(infoHandler.getInfo());
        info.add(MekanismLang.UNIT.translate(MekanismConfig.common.tempUnit.get()));
        if (!info.equals(lastInfo)) {
            lastInfo = info;
            lastTooltip = TooltipUtils.create(info);
        }
        setTooltip(lastTooltip);
    }

    @Override
    protected Identifier getResource() {
        return ICONS.computeIfAbsent(MekanismConfig.common.tempUnit.get(), type -> MekanismUtils.getResource(ResourceType.GUI_TAB,
              "heat_info_" + type.getTabName() + ".png"));
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        int button = event.button();
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            updateTemperatureUnit(IIncrementalEnum::getNext);
        } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            updateTemperatureUnit(IIncrementalEnum::getPrevious);
        }
    }

    @Override
    public boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return buttonInfo.button() == InputConstants.MOUSE_BUTTON_LEFT || buttonInfo.button() == InputConstants.MOUSE_BUTTON_RIGHT;
    }

    private void updateTemperatureUnit(UnaryOperator<TemperatureUnit> converter) {
        TemperatureUnit current = MekanismConfig.common.tempUnit.get();
        TemperatureUnit updated = converter.apply(current);
        if (current != updated) {//Should always be true but validate it
            MekanismConfig.common.tempUnit.set(updated);
            MekanismConfig.common.save();
        }
    }
}