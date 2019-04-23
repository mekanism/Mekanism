package mekanism.client.gui.element;

import java.awt.Rectangle;
import java.util.List;
import mekanism.client.gui.IGuiWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiElement {

    public static Minecraft mc = Minecraft.getMinecraft();

    protected final ResourceLocation RESOURCE;
    protected final IGuiWrapper guiObj;
    protected final ResourceLocation defaultLocation;

    public GuiElement(ResourceLocation resource, IGuiWrapper gui, ResourceLocation def) {
        RESOURCE = resource;
        guiObj = gui;
        defaultLocation = def;
    }

    public void displayTooltip(String s, int xAxis, int yAxis) {
        guiObj.displayTooltip(s, xAxis, yAxis);
    }

    public void displayTooltips(List<String> list, int xAxis, int yAxis) {
        guiObj.displayTooltips(list, xAxis, yAxis);
    }

    public void offsetX(int xSize) {
        if (guiObj instanceof GuiContainer) {
            ((GuiContainer) guiObj).xSize += xSize;
        }
    }

    public void offsetY(int ySize) {
        if (guiObj instanceof GuiContainer) {
            ((GuiContainer) guiObj).ySize += ySize;
        }
    }

    public void offsetLeft(int guiLeft) {
        if (guiObj instanceof GuiContainer) {
            ((GuiContainer) guiObj).guiLeft += guiLeft;
        }
    }

    public void offsetTop(int guiTop) {
        if (guiObj instanceof GuiContainer) {
            ((GuiContainer) guiObj).guiTop += guiTop;
        }
    }

    public void renderScaledText(String text, int x, int y, int color, int maxX) {
        int length = getFontRenderer().getStringWidth(text);

        if (length <= maxX) {
            getFontRenderer().drawString(text, x, y, color);
        } else {
            float scale = (float) maxX / length;
            float reverse = 1 / scale;
            float yAdd = 4 - (scale * 8) / 2F;

            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale);
            getFontRenderer().drawString(text, (int) (x * reverse), (int) ((y * reverse) + yAdd), color);
            GlStateManager.popMatrix();
        }
    }

    public FontRenderer getFontRenderer() {
        return guiObj.getFont();
    }

    public void mouseClickMove(int mouseX, int mouseY, int button, long ticks) {
    }

    public void mouseReleased(int x, int y, int type) {
    }

    public void mouseWheel(int x, int y, int delta) {
    }

    public abstract Rectangle4i getBounds(int guiWidth, int guiHeight);

    public abstract void renderBackground(int xAxis, int yAxis, int guiWidth, int guiHeight);

    public abstract void renderForeground(int xAxis, int yAxis);

    public abstract void preMouseClicked(int xAxis, int yAxis, int button);

    public abstract void mouseClicked(int xAxis, int yAxis, int button);

    public interface IInfoHandler {

        List<String> getInfo();
    }

    public static class Rectangle4i {

        public int x, y, width, height;

        public Rectangle4i(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Rectangle toRectangle() {
            return new Rectangle(x, y, width, height);
        }
    }

    protected boolean inBounds(int xAxis, int yAxis) {
        return false;
    }
}