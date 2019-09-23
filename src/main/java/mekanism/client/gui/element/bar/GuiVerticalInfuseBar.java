package mekanism.client.gui.element.bar;

import javax.annotation.Nonnull;
import mekanism.api.infuse.InfuseType;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.bar.GuiVerticalInfuseBar.InfuseInfoProvider;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;
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
        InfuseType type = getHandler().getType();
        if (!type.isEmptyType()) {
            int displayInt = (int) (getHandler().getLevel() * texHeight);
            MekanismRenderer.color(type);
            guiObj.drawTexturedRectFromIcon(x + 1, y + 1 + (texHeight - displayInt), type.getSprite(), texWidth, displayInt);
            MekanismRenderer.resetColor();
        }
    }

    //Note the GuiBar.IBarInfoHandler is needed, as it cannot compile and resolve just IBarInfoHandler
    public interface InfuseInfoProvider extends GuiVerticalBar.IBarInfoHandler {

        @Nonnull
        InfuseType getType();
    }
}