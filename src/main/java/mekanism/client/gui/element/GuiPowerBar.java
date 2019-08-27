package mekanism.client.gui.element;

import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiPowerBar extends GuiElement {

    private final IStrictEnergyStorage tileEntity;
    private final IPowerInfoHandler handler;

    //TODO: For this and elements like it we should not allow clicking them even if the on click does nothing (we don't want a click sound to be made)
    public GuiPowerBar(IGuiWrapper gui, IStrictEnergyStorage tile, ResourceLocation def, int x, int y) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "power_bar.png"), gui, def, x, y, 6, 56);
        tileEntity = tile;
        handler = new IPowerInfoHandler() {
            @Override
            public ITextComponent getTooltip() {
                return EnergyDisplay.of(tileEntity.getEnergy(), tileEntity.getMaxEnergy()).getTextComponent();
            }

            @Override
            public double getLevel() {
                return tileEntity.getEnergy() / tileEntity.getMaxEnergy();
            }
        };
    }

    public GuiPowerBar(IGuiWrapper gui, IPowerInfoHandler h, ResourceLocation def, int x, int y) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "power_bar.png"), gui, def, x, y, 6, 56);
        tileEntity = null;
        handler = h;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(RESOURCE);
        guiObj.drawTexturedRect(x, y, 0, 0, width, height);
        if (handler.getLevel() > 0) {
            int displayInt = (int) (handler.getLevel() * 52) + 2;
            guiObj.drawTexturedRect(x, y + height - displayInt, 6, height - displayInt, width, displayInt);
        }
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        ITextComponent tooltip = handler.getTooltip();
        if (tooltip != null) {
            displayTooltip(tooltip, mouseX, mouseY);
        }
    }

    public static abstract class IPowerInfoHandler {

        public ITextComponent getTooltip() {
            return null;
        }

        public abstract double getLevel();
    }
}