package mekanism.client.gui.element.bar;

import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiBar.IBarInfoHandler;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiPowerBar extends GuiBar<IBarInfoHandler> {

    private static final ResourceLocation ENERGY_BAR = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "power_bar.png");

    //TODO: For this and elements like it we should not allow clicking them even if the on click does nothing (we don't want a click sound to be made)
    public GuiPowerBar(IGuiWrapper gui, IStrictEnergyStorage tile, ResourceLocation def, int x, int y) {
        super(ENERGY_BAR, gui, new IBarInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return EnergyDisplay.of(tile.getEnergy(), tile.getMaxEnergy()).getTextComponent();
            }

            @Override
            public double getLevel() {
                return tile.getEnergy() / tile.getMaxEnergy();
            }
        }, def, x, y, 6, 56);
    }

    public GuiPowerBar(IGuiWrapper gui, IBarInfoHandler handler, ResourceLocation def, int x, int y) {
        super(ENERGY_BAR, gui, handler, def, x, y, 6, 56);
    }

    @Override
    protected void renderBarOverlay(int mouseX, int mouseY, float partialTicks) {
        int displayInt = (int) (getHandler().getLevel() * 52);
        guiObj.drawModalRectWithCustomSizedTexture(x + 1, y - 2+ height - displayInt, 0, 0, 4, displayInt, 4, 52);
    }
}