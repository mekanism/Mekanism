package mekanism.client.gui;

import java.util.List;
import java.util.stream.Collectors;
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

    void displayTooltip(String s, int xAxis, int yAxis);

    default void displayTooltip(ITextComponent component, int xAxis, int yAxis) {
        //TODO: Replace displayTooltip(String with this?
        displayTooltip(component.getFormattedText(), xAxis, yAxis);
    }

    void displayTooltips(List<String> list, int xAxis, int yAxis);

    default void displayComponentTooltips(List<ITextComponent> list, int xAxis, int yAxis) {
        //TODO: See how much this method can be used. Maybe even entirely replace displayTooltips with it
        displayTooltips(list.stream().map(ITextComponent::getFormattedText).collect(Collectors.toList()), xAxis, yAxis);
    }

    @Nullable
    FontRenderer getFont();
}