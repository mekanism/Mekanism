package mekanism.client.gui;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IGuiWrapper {

    void drawTexturedRect(int x, int y, int u, int v, int w, int h);

    void drawTexturedRectFromIcon(int x, int y, TextureAtlasSprite icon, int w, int h);

    void displayTooltip(ITextComponent component, int xAxis, int yAxis);

    void displayTooltips(List<ITextComponent> list, int xAxis, int yAxis);

    @Nullable
    FontRenderer getFont();
}