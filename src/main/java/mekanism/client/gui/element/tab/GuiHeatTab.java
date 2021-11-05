package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.UnitDisplayUtils.TempType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiHeatTab extends GuiBiDirectionalTab {

    private static final Map<TempType, ResourceLocation> ICONS = new EnumMap<>(TempType.class);
    private final IInfoHandler infoHandler;

    public GuiHeatTab(IGuiWrapper gui, IInfoHandler handler) {
        super(MekanismUtils.getResource(ResourceType.GUI_TAB, "heat_info.png"), gui, -26, 109, 26, 26);
        infoHandler = handler;
    }

    @Override
    public void drawBackground(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(matrix, mouseX, mouseY, partialTicks);
        minecraft.textureManager.bind(getResource());
        blit(matrix, x, y, 0, 0, width, height, width, height);
    }

    @Override
    public void renderToolTip(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        super.renderToolTip(matrix, mouseX, mouseY);
        List<ITextComponent> info = new ArrayList<>(infoHandler.getInfo());
        info.add(MekanismLang.UNIT.translate(MekanismConfig.general.tempUnit.get()));
        displayTooltips(matrix, info, mouseX, mouseY);
    }

    @Override
    protected ResourceLocation getResource() {
        return ICONS.computeIfAbsent(MekanismConfig.general.tempUnit.get(), type -> MekanismUtils.getResource(ResourceType.GUI_TAB,
              "heat_info_" + type.name().toLowerCase(Locale.ROOT) + ".png"));
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        MekanismConfig.general.tempUnit.set(MekanismConfig.general.tempUnit.get().getNext());
    }


    @Override
    protected void onRightClick(double mouseX, double mouseY) {
        MekanismConfig.general.tempUnit.set(MekanismConfig.general.tempUnit.get().getPrevious());
    }
}