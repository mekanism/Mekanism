package mekanism.client.gui.element;

import java.util.ArrayList;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.text.ITextComponent;

public class GuiHeatInfo extends GuiTexturedElement {

    private final IInfoHandler infoHandler;

    public GuiHeatInfo(IInfoHandler handler, IGuiWrapper gui) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "heat_info.png"), gui, -26, 112, 26, 26);
        infoHandler = handler;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        guiObj.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        List<ITextComponent> info = new ArrayList<>(infoHandler.getInfo());
        info.add(MekanismLang.UNIT.translate(MekanismConfig.general.tempUnit.get()));
        displayTooltips(info, mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        MekanismConfig.general.tempUnit.set(MekanismConfig.general.tempUnit.get().getNext());
    }
}