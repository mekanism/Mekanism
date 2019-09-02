package mekanism.client.gui.element.bar;

import javax.annotation.Nullable;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiInfuseBar.InfuseInfoProvider;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiInfuseBar extends GuiBar<InfuseInfoProvider> {

    public GuiInfuseBar(IGuiWrapper gui, InfuseInfoProvider infoProvider, ResourceLocation def, int x, int y) {
        super(gui, infoProvider, def, x, y, 6, 56);
    }

    @Override
    protected void renderBarOverlay(int mouseX, int mouseY, float partialTicks) {
        TextureAtlasSprite sprite = getHandler().getSprite();
        if (sprite != null) {
            minecraft.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            int displayInt = (int) (getHandler().getLevel() * 52);
            guiObj.drawTexturedRectFromIcon(x + 1, y - 2 + height - displayInt, sprite, 4, displayInt);
        }
    }

    //Note the GuiBar.IBarInfoHandler is needed, as it cannot compile and resolve just IBarInfoHandler
    public interface InfuseInfoProvider extends GuiBar.IBarInfoHandler {

        @Nullable
        TextureAtlasSprite getSprite();
    }
}