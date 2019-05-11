package mekanism.client.gui;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface IGuiWrapper {

    void drawTexturedRect(int x, int y, int u, int v, int w, int h);

    void drawTexturedRectFromIcon(int x, int y, TextureAtlasSprite icon, int w, int h);

    void displayTooltip(String s, int xAxis, int yAxis);

    void displayTooltips(List<String> list, int xAxis, int yAxis);

    @Nullable
    FontRenderer getFont();
}