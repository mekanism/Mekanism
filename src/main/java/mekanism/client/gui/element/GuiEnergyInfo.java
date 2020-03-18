package mekanism.client.gui.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.util.text.ITextComponent;

public class GuiEnergyInfo extends GuiTexturedElement {

    private final IInfoHandler infoHandler;

    //TODO: Look at usages of this and see if any can pass the energy container one instead
    public GuiEnergyInfo(IInfoHandler handler, IGuiWrapper gui) {
        super(MekanismUtils.getResource(ResourceType.GUI, "energy_info.png"), gui, -26, 138, 26, 26);
        infoHandler = handler;
    }

    public GuiEnergyInfo(MachineEnergyContainer<?> energyContainer, IGuiWrapper gui) {
        this(() -> Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(energyContainer.getEnergyPerTick())),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(energyContainer.getNeeded()))), gui);
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        blit(x, y, 0, 0, width, height, width, height);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        List<ITextComponent> info = new ArrayList<>(infoHandler.getInfo());
        info.add(MekanismLang.UNIT.translate(MekanismConfig.general.energyUnit.get()));
        displayTooltips(info, mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        MekanismConfig.general.energyUnit.set(MekanismConfig.general.energyUnit.get().getNext());
    }
}