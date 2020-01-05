package mekanism.client.gui;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;

public interface IGuiWrapper {

    void drawTexturedRect(int x, int y, int textureX, int textureY, int width, int height);

    void drawTexturedRectFromIcon(int x, int y, TextureAtlasSprite icon, int width, int height);

    //TODO: Rename
    void drawModalRectWithCustomSizedTexture(int x, int y, int textureX, int textureY, int width, int height, int textureWidth, int textureHeight);

    //TODO: Rename
    void drawModalRectWithCustomSizedTexture(int x, int y, int desiredWidth, int desiredHeight, int textureX, int textureY, int width, int height, int textureWidth, int textureHeight);

    void displayTooltip(ITextComponent component, int xAxis, int yAxis);

    void displayTooltips(List<ITextComponent> list, int xAxis, int yAxis);

    default int getLeft() {
        if (this instanceof ContainerScreen) {
            return ((ContainerScreen<?>) this).getGuiLeft();
        }
        return 0;
    }

    default int getTop() {
        if (this instanceof ContainerScreen) {
            return ((ContainerScreen<?>) this).getGuiTop();
        }
        return 0;
    }

    default int getWidth() {
        if (this instanceof ContainerScreen) {
            return ((ContainerScreen<?>) this).getXSize();
        }
        return 0;
    }

    default int getHeight() {
        if (this instanceof ContainerScreen) {
            return ((ContainerScreen<?>) this).getYSize();
        }
        return 0;
    }

    @Nullable
    FontRenderer getFont();
}