package mekanism.client.gui.element.tab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTexturedElement;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class GuiEnergyTab extends GuiTexturedElement {

    private final IInfoHandler infoHandler;

    private List<Component> lastInfo = Collections.emptyList();
    @Nullable
    private Tooltip lastTooltip;

    public GuiEnergyTab(IGuiWrapper gui, IInfoHandler handler) {
        super(MekanismUtils.getResource(ResourceType.GUI_TAB, "energy_info.png"), gui, -26, 137, 26, 26);
        infoHandler = handler;
    }

    public GuiEnergyTab(IGuiWrapper gui, MachineEnergyContainer<?> energyContainer, LongSupplier lastEnergyUsed) {
        this(gui, () -> List.of(MekanismLang.USING.translate(EnergyDisplay.of(lastEnergyUsed.getAsLong())),
              MekanismLang.NEEDED.translate(EnergyDisplay.of(energyContainer.getNeededAsLong()))));
    }

    //TODO: Re-evaluate uses of this constructor at some point
    public GuiEnergyTab(IGuiWrapper gui, MachineEnergyContainer<?> energyContainer, BooleanSupplier isActive) {
        this(gui, () -> {
            //Note: This isn't the most accurate using calculation as deactivation doesn't sync instantly
            // to the client, but it is close enough given a lot more things would have to be kept track of otherwise
            // which would lead to higher memory usage
            int using = isActive.getAsBoolean() ? energyContainer.getEnergyPerTick() : 0;
            return List.of(MekanismLang.USING.translate(EnergyDisplay.of(using)),
                  MekanismLang.NEEDED.translate(EnergyDisplay.of(energyContainer.getNeededAsLong())));
        });
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, getResource(), relativeX, relativeY, 0, 0, width, height, width, height);
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        List<Component> info = new ArrayList<>(infoHandler.getInfo());
        info.add(MekanismLang.UNIT.translate(MekanismLang.ENERGY_FORGE_SHORT));
        if (!info.equals(lastInfo)) {
            lastInfo = info;
            lastTooltip = TooltipUtils.create(info);
        }
        setTooltip(lastTooltip);
    }
}