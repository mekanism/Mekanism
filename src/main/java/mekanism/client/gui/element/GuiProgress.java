package mekanism.client.gui.element;

import mekanism.client.gui.IGuiWrapper;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiProgress extends GuiElement {

    private final IProgressInfoHandler handler;
    private final ProgressBar type;

    public GuiProgress(IProgressInfoHandler handler, ProgressBar type, IGuiWrapper gui, ResourceLocation def, int x, int y) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "progress.png"), gui, def, x, y, type.width, type.height);
        this.type = type;
        this.handler = handler;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        minecraft.textureManager.bindTexture(RESOURCE);
        if (handler.isActive()) {
            guiObj.drawTexturedRect(x, y, type.textureX, type.textureY, width, height);
            int innerOffsetX = 2;
            int displayInt = (int) (handler.getProgress() * (width - 2 * innerOffsetX));
            guiObj.drawTexturedRect(x + innerOffsetX, y, type.textureX + width + innerOffsetX, type.textureY, displayInt, height);
        }
        minecraft.textureManager.bindTexture(defaultLocation);
    }

    public enum ProgressBar {
        BLUE(28, 11, 0, 0),
        YELLOW(28, 11, 0, 11),
        RED(28, 11, 0, 22),
        GREEN(28, 11, 0, 33),
        PURPLE(28, 11, 0, 44),
        STONE(28, 11, 0, 55),
        CRUSH(28, 11, 0, 66),

        LARGE_RIGHT(52, 10, 128, 0),
        LARGE_LEFT(52, 10, 128, 10),
        MEDIUM(36, 10, 128, 20),
        SMALL_RIGHT(32, 10, 128, 30),
        SMALL_LEFT(32, 10, 128, 40),
        BI(20, 8, 128, 50);

        public final int width;
        public final int height;

        public final int textureX;
        public final int textureY;

        ProgressBar(int w, int h, int u, int v) {
            width = w;
            height = h;
            textureX = u;
            textureY = v;
        }
    }

    public static abstract class IProgressInfoHandler {

        public abstract double getProgress();

        public boolean isActive() {
            return true;
        }
    }
}