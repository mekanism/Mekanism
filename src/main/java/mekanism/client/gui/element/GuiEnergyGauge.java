package mekanism.client.gui.element;

import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEnergyGauge extends GuiGauge {

    private final IEnergyInfoHandler infoHandler;

    public GuiEnergyGauge(IEnergyInfoHandler handler, Type type, IGuiWrapper gui, ResourceLocation def, int x, int y) {
        super(type, gui, def, x, y);
        infoHandler = handler;
    }

    @Override
    public Rectangle4i getBounds(int guiWidth, int guiHeight) {
        return new Rectangle4i(guiWidth - 26, guiHeight + 6, 26, 26);
    }

    @Override
    public TransmissionType getTransmission() {
        return TransmissionType.ENERGY;
    }

    @Override
    public int getScaledLevel() {
        if (infoHandler.getEnergyStorage().getEnergy() == Double.MAX_VALUE) {
            return height - 2;
        }

        return (int) (infoHandler.getEnergyStorage().getEnergy() * (height - 2) / infoHandler.getEnergyStorage()
              .getMaxEnergy());
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return MekanismRenderer.energyIcon;
    }

    @Override
    public String getTooltipText() {
        return infoHandler.getEnergyStorage().getEnergy() > 0 ? MekanismUtils
              .getEnergyDisplay(infoHandler.getEnergyStorage().getEnergy(),
                    infoHandler.getEnergyStorage().getMaxEnergy()) : LangUtils.localize("gui.empty");
    }

    public interface IEnergyInfoHandler {

        IStrictEnergyStorage getEnergyStorage();
    }
}