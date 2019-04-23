package mekanism.client.gui.element;

import static java.lang.Math.min;

import mekanism.api.transmitters.TransmissionType;
import mekanism.client.gui.IGuiWrapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiNumberGauge extends GuiGauge {

    private final INumberInfoHandler infoHandler;

    public GuiNumberGauge(INumberInfoHandler handler, Type type, IGuiWrapper gui, ResourceLocation def, int x, int y) {
        super(type, gui, def, x, y);
        infoHandler = handler;
    }

    @Override
    public TransmissionType getTransmission() {
        return null;
    }

    @Override
    public int getScaledLevel() {
        return (int) ((height - 2) * min(infoHandler.getLevel() / infoHandler.getMaxLevel(), 1));
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return infoHandler.getIcon();
    }

    @Override
    public String getTooltipText() {
        return infoHandler.getText(infoHandler.getLevel());
    }


    public interface INumberInfoHandler {

        TextureAtlasSprite getIcon();

        double getLevel();

        double getMaxLevel();

        String getText(double level);
    }
}