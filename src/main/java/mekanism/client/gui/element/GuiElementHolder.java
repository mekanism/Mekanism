package mekanism.client.gui.element;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import mekanism.client.gui.IGuiWrapper;
import mekanism.common.Mekanism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public class GuiElementHolder extends GuiElement {

    public static final Identifier HOLDER = Mekanism.rl("element_holder");
    private static final int HOLDER_BORDER_SIZE = 1;
    private static int BACKGROUND_COLOR = 0xFF787878;

    public GuiElementHolder(IGuiWrapper gui, int x, int y, int width, int height) {
        super(gui, x, y, width, height);
        active = false;
    }

    @Override
    public void renderWidget(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOLDER, getButtonX(), getButtonY(), getButtonWidth(), getButtonHeight());
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
    }

    public static int getBackgroundColor() {
        return BACKGROUND_COLOR;
    }

    public static void updateBackgroundColor() {
        //TODO: Try to do this in a more generic way. We don't directly use our ColorAtlas because we want to automatically
        // get it from the texture
        //TODO - 26.2: get it from the gui sprites atlas instead
        try (InputStream stream = Minecraft.getInstance().getResourceManager().open(HOLDER.withPrefix("textures/gui/sprites/").withSuffix(".png"));
             NativeImage image = NativeImage.read(stream)) {
            int argb = image.getPixel(HOLDER_BORDER_SIZE + 1, HOLDER_BORDER_SIZE + 1);
            if (ARGB.alpha(argb) == 0) {
                //Don't allow fully transparent colors, fallback to default color.
                // Mark as null for now so that it can default to the proper color
                argb = 0xFF787878;
                Mekanism.logger.warn("Unable to retrieve background color for element holder.");
            }
            BACKGROUND_COLOR = argb;
        } catch (Exception e) {
            Mekanism.logger.error("Failed to retrieve background color for element holder", e);
        }
    }
}