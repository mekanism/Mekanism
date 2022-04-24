package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiHeatTab extends GuiBiDirectionalTab {

    private static final Map<TemperatureUnit, ResourceLocation> ICONS = new EnumMap<>(TemperatureUnit.class);
    private final IInfoHandler infoHandler;

    public GuiHeatTab(IGuiWrapper gui, IInfoHandler handler) {
        super(MekanismUtils.getResource(ResourceType.GUI_TAB, "heat_info.png"), gui, -26, 109, 26, 26);
        infoHandler = handler;
    }

    @Override
    public void drawBackground(@Nonnull PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        RenderSystem.setShaderTexture(0, getResource());
        blit(matrix, x, y, 0, 0, width, height, width, height);
    }

    @Override
    public void renderToolTip(@Nonnull PoseStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        List<Component> info = new ArrayList<>(infoHandler.getInfo());
        info.add(MekanismLang.UNIT.translate(MekanismConfig.common.tempUnit.get()));
        displayTooltips(matrix, mouseX, mouseY, info);
    }

    @Override
    protected ResourceLocation getResource() {
        return ICONS.computeIfAbsent(MekanismConfig.common.tempUnit.get(), type -> MekanismUtils.getResource(ResourceType.GUI_TAB,
              "heat_info_" + type.getTabName() + ".png"));
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        updateTemperatureUnit(IIncrementalEnum::getNext);
    }


    @Override
    protected void onRightClick(double mouseX, double mouseY) {
        updateTemperatureUnit(IIncrementalEnum::getPrevious);
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