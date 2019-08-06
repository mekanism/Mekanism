package mekanism.client.gui.element.tab;

import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiTileEntityElement;
import mekanism.client.sound.SoundHandler;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiTabElement<TILE extends TileEntity> extends GuiTileEntityElement<TILE> {

    protected final int yPos;

    public GuiTabElement(ResourceLocation resource, IGuiWrapper gui, ResourceLocation def, TILE tile, int y) {
        super(resource, gui, def, tile);
        yPos = y;
    }

    public abstract void displayForegroundTooltip(int xAxis, int yAxis);

    public abstract void buttonClicked();

    @Override
    public Rectangle4i getBounds(int guiWidth, int guiHeight) {
        return new Rectangle4i(guiWidth - 26, guiHeight + yPos, 26, 26);
    }

    @Override
    protected boolean inBounds(int xAxis, int yAxis) {
        return xAxis >= -21 && xAxis <= -3 && yAxis >= yPos + 4 && yAxis <= yPos + 22;
    }

    @Override
    public void mouseClicked(int xAxis, int yAxis, int button) {
        if (button == 0 && inBounds(xAxis, yAxis)) {
            buttonClicked();
            SoundHandler.playSound(SoundEvents.UI_BUTTON_CLICK);
        }
    }

    @Override
    public void preMouseClicked(int xAxis, int yAxis, int button) {
    }

    @Override
    public void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight) {
        mc.renderEngine.bindTexture(RESOURCE);
        guiObj.drawTexturedRect(guiWidth - 26, guiHeight + yPos, 0, 0, 26, 26);
        guiObj.drawTexturedRect(guiWidth - 21, guiHeight + yPos + 4, 26, inBounds(xAxis, yAxis) ? 0 : 18, 18, 18);
        mc.renderEngine.bindTexture(defaultLocation);
    }

    @Override
    public void renderForeground(int xAxis, int yAxis) {
        mc.renderEngine.bindTexture(RESOURCE);
        if (inBounds(xAxis, yAxis)) {
            displayForegroundTooltip(xAxis, yAxis);
        }
        mc.renderEngine.bindTexture(defaultLocation);
    }
}