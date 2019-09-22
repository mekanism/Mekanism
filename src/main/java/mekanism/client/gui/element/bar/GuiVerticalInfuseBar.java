package mekanism.client.gui.element.bar;

import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiVerticalInfuseBar.InfuseInfoProvider;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiVerticalInfuseBar extends GuiVerticalBar<InfuseInfoProvider> {

    private static final int texWidth = 4;
    private static final int texHeight = 52;

    public GuiVerticalInfuseBar(IGuiWrapper gui, InfuseInfoProvider infoProvider, ResourceLocation def, int x, int y) {
        super(AtlasTexture.LOCATION_BLOCKS_TEXTURE, gui, infoProvider, def, x, y, texWidth + 2, texHeight + 2);
    }

    @Override
    protected void renderBarOverlay(int mouseX, int mouseY, float partialTicks) {
        TextureAtlasSprite sprite = getHandler().getSprite();
        if (sprite != null) {
            int displayInt = (int) (getHandler().getLevel() * texHeight);
            MekanismRenderer.color(getHandler().getTint());
            guiObj.drawTexturedRectFromIcon(x + 1, y + 1 + (texHeight - displayInt), sprite, texWidth, displayInt);
            MekanismRenderer.resetColor();
        }
    }

    //Note the GuiBar.IBarInfoHandler is needed, as it cannot compile and resolve just IBarInfoHandler
    public interface InfuseInfoProvider extends GuiVerticalBar.IBarInfoHandler {

        @Nullable
        TextureAtlasSprite getSprite();

        int getTint();
    }
}