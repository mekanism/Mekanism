package mekanism.client.gui.element.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.util.text.ITextComponent;

public class GuiEnergyTab extends GuiTexturedElement {

    private final IInfoHandler infoHandler;

    public GuiEnergyTab(IInfoHandler handler, IGuiWrapper gui) {
        super(MekanismUtils.getResource(ResourceType.GUI, "energy_info.png"), gui, -26, 138, 26, 26);
        infoHandler = handler;
    }

    public GuiEnergyTab(MachineEnergyContainer<?> energyContainer, IGuiWrapper gui) {
        this(() -> Arrays.asList(MekanismLang.USING.translate(EnergyDisplay.of(energyContainer.getEnergyPerTick())),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(energyContainer.getNeeded()))), gui);
    }

    @Override
    public void func_230431_b_(@Nonnull MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(getResource());
        func_238463_a_(matrix, field_230690_l_, field_230691_m_, 0, 0, field_230688_j_, field_230689_k_, field_230688_j_, field_230689_k_);
    }

    @Override
    public void func_230443_a_(@Nonnull MatrixStack matrix, int mouseX, int mouseY) {
        List<ITextComponent> info = new ArrayList<>(infoHandler.getInfo());
        info.add(MekanismLang.UNIT.translate(MekanismConfig.general.energyUnit.get()));
        displayTooltips(matrix, info, mouseX, mouseY);
    }

    @Override
    public void func_230982_a_(double mouseX, double mouseY) {
        MekanismConfig.general.energyUnit.set(MekanismConfig.general.energyUnit.get().getNext());
    }
}